package com.sky.mq.converter;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ImageMessageConverter implements MessageConverter {


    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        throw  new MessageConversionException("IMAGE转换异常!");
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        System.out.println("---------------IMAGE转换------------------");
        byte[] body = message.getBody();
        Object _extName = message.getMessageProperties().getHeaders().get("extName");
        String extName = _extName == null ? "png":_extName.toString();

        String fileName = UUID.randomUUID().toString();
        String path = "d:/RQ_FILE/" + fileName +"." + extName;
        File file = new File(path);

        try {
            Files.copy(new ByteArrayInputStream(body), file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
