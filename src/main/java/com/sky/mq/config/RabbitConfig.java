package com.sky.mq.config;

import com.sky.mq.adaptor.MessageDelegate;
import com.sky.mq.converter.ImageMessageConverter;
import com.sky.mq.converter.PDFMessageConverter;
import com.sky.mq.converter.TextMessageConverter;
import com.sky.mq.model.Order;
import com.sky.mq.model.Packaged;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class RabbitConfig {

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("49.234.60.118");
        connectionFactory.setUsername("sky-test-01");
        connectionFactory.setPassword("sky-test-01");
        connectionFactory.setVirtualHost("host-test-01");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        return new RabbitTemplate(connectionFactory);
    }


    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        simpleMessageListenerContainer.addQueues(
                queue001(),queue002(),queue003(),queueImage(),queuePdf()
        );
        simpleMessageListenerContainer.setConcurrentConsumers(1);
        simpleMessageListenerContainer.setMaxConcurrentConsumers(10);
        // 设置是否重回队列，一般为false
        simpleMessageListenerContainer.setDefaultRequeueRejected(false);
        // 设置签收模式
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        // 消费端的标签生成策略
        simpleMessageListenerContainer.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                return queue + "_" + UUID.randomUUID().toString();
            }
        });
        // 下面是监听器的方式
        /*simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                String msg = new String(message.getBody());
                System.out.println("----------消费者："+msg);
            }
        });*/

        //下面是适配器的方式
        /*MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        messageListenerAdapter.setMessageConverter(new TextMessageConverter());
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/

        // 下面是适配器的方式:队列名称和方法名称一一匹配
       /* MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());
        Map<String,String> map = new HashMap<>();
        map.put("queue-001","method1");
        map.put("queue-002","method2");
        messageListenerAdapter.setQueueOrTagToMethodName(map);
        messageListenerAdapter.setMessageConverter(new TextMessageConverter());
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
*/
       // 1.1 支持json格式的转换器
        /*MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());

        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/

        // 1.2 DefaultJackson2JavaTypeMapper & Jackson2JsonMessageConverter支持java对象转换
        /*MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setJavaTypeMapper(new DefaultJackson2JavaTypeMapper());
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/

        // 1.3 DefaultJackson2JavaTypeMapper & Jackson2JsonMessageConverter 支持java对象多映射转换
       /* MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();

        DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();

        Map<String,Class<?>> map = new HashMap<>();
        map.put("order", Order.class);
       map.put("packaged", Packaged.class);

        javaTypeMapper.setIdClassMapping(map);

        jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/


       // 1.4 扩展Convert:全局的转换器
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        ContentTypeDelegatingMessageConverter converter = new ContentTypeDelegatingMessageConverter();

        TextMessageConverter textMessageConverter = new TextMessageConverter();
        converter.addDelegate("text",textMessageConverter);
        converter.addDelegate("html/text",textMessageConverter);
        converter.addDelegate("xml/text",textMessageConverter);
        converter.addDelegate("text/plain",textMessageConverter);

        Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
        converter.addDelegate("json",jsonMessageConverter);
        converter.addDelegate("application/json",jsonMessageConverter);

        ImageMessageConverter imageMessageConverter = new ImageMessageConverter();
        converter.addDelegate("image",imageMessageConverter);
        converter.addDelegate("image/png",imageMessageConverter);

        PDFMessageConverter pdfMessageConverter = new PDFMessageConverter();
        converter.addDelegate("application/pdf",pdfMessageConverter);
        messageListenerAdapter.setMessageConverter(converter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
        return simpleMessageListenerContainer;
    }

    @Bean
    public TopicExchange exchange001(){
        return new TopicExchange("exchange-topic-001",true,false);
    }


    @Bean
    public Queue queue001(){
       return new Queue("queue-001",true);
    }

    @Bean
    public Binding binding001(){
        return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.*");
    }

    @Bean
    public TopicExchange exchange002(){
        return new TopicExchange("exchange-topic-002",true,false);
    }

    @Bean
    public Queue queue002(){
        return new Queue("queue-002",true);
    }

    @Bean
    public Binding binding002(){
        return BindingBuilder.bind(queue002()).to(exchange002()).with("rabbit.*");
    }

    @Bean
    public Queue queue003(){
        return new Queue("queue-003",true);
    }

    @Bean
    public Binding binding003(){
        return BindingBuilder.bind(queue003()).to(exchange001()).with("mq.*");
    }


    @Bean
    public Queue queueImage(){
        return new Queue("image_queue",true);
    }

    @Bean
    public Queue queuePdf(){
        return new Queue("pdf_queue",true);
    }
}
