package com.sky.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.mq.model.Order;
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

import java.nio.file.Files;
import java.nio.file.Paths;

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


    @Test
    public void testRabbitTemplate2() {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getHeaders().put("desc", "信息描述");
        messageProperties.getHeaders().put("type", "自定义消息类型");
        messageProperties.setContentType("text/plain");

        Message message = new Message("Hello RabbitMQ----1111".getBytes(), messageProperties);
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


        message = new Message("Hello RabbitMQ---2222".getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("exchange-topic-002", "rabbit.msg", message, new MessagePostProcessor() {
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


    @Test
    public void testSendJsonMessage() throws Exception {
        Order order = new Order();
        order.setId("S001");
        order.setName("订单消息");
        order.setContent("描述信息");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(order);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(json.getBytes(),messageProperties);
        rabbitTemplate.send("exchange-topic-001","spring.order",message);
    }


    @Test
    public void testSendJavaMessage() throws Exception {
        Order order = new Order();
        order.setId("S001");
        order.setName("订单消息");
        order.setContent("描述信息");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(order);

        System.out.println("---->json:"+json);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "com.sky.mq.model.Order");
        Message message = new Message(json.getBytes(),messageProperties);
        rabbitTemplate.send("exchange-topic-001","spring.order",message);
    }

    @Test
    public void testSendJavaMessage2() throws Exception {
        Order order = new Order();
        order.setId("S001");
        order.setName("订单消息");
        order.setContent("描述信息");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(order);

        System.out.println("---->json:"+json);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "order");
        Message message = new Message(json.getBytes(),messageProperties);
        rabbitTemplate.send("exchange-topic-001","spring.order",message);
    }


    @Test
    public void testSendExtConverterMessage() throws Exception {

        byte[] body = Files.readAllBytes(Paths.get("d:/","szw.png"));
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("image/png");
        messageProperties.getHeaders().put("extName","jpg");
        Message message = new Message(body,messageProperties);
        rabbitTemplate.send("","image_queue",message);
    }



    @Test
    public void testSendExtConverterMessage2() throws Exception {

        byte[] body = Files.readAllBytes(Paths.get("d:/","史战伟简历.pdf"));
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/pdf");
        Message message = new Message(body,messageProperties);
        rabbitTemplate.send("","pdf_queue",message);
    }
}
