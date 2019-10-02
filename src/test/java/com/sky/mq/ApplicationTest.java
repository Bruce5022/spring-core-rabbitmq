package com.sky.mq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testRabbitAdmin() {

        rabbitAdmin.declareExchange(new DirectExchange("test.direct", false, false));
        rabbitAdmin.declareExchange(new TopicExchange("test.topic", false, false));
        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", false, false));

        rabbitAdmin.declareQueue(new Queue("test.direct.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.topic.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.fanout.queue", false));

        rabbitAdmin.declareBinding(new Binding("test.direct.queue",
                Binding.DestinationType.QUEUE,
                "test.direct",
                "direct",
                null));

        rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue("test.topic.queue", false))//直接创建队列
                .to(new TopicExchange("test.topic", false, false))//直接创建交换机 建立关联关系
                .with("user.#")//指定路由key
        );


        // fanout不需要routing-key
        rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue("test.fanout.queue", false))//直接创建队列
                .to(new FanoutExchange("test.fanout", false, false))//直接创建交换机 建立关联关系
        );

        // 清空队列
        rabbitAdmin.purgeQueue("test.topic.queue", false);
    }


    @Test
    public void testRabbitTemplate() {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getHeaders().put("desc", "信息描述");
        messageProperties.getHeaders().put("type", "自定义消息类型");

        Message message = new Message("Hello RabbitMQ".getBytes(), messageProperties);

        rabbitTemplate.convertAndSend("exchange-topic-001", "spring.msg", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                System.out.println("处理消息...");
                message.getMessageProperties().getHeaders().put("desc", "已经修改的信息描述");
                message.getMessageProperties().getHeaders().put("attr", "新增属性");
                System.out.println("处理消息end");
                return message;
            }
        });
    }
}
