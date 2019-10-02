package com.sky.mq.adaptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.mq.model.Order;
import com.sky.mq.model.Packaged;

import java.io.File;
import java.util.Map;

public class MessageDelegate {

  /*  public void handleMessage(byte[] messageBody) {
        System.out.println("默认方法，消息内容：" + new String(messageBody));
    }*/

 /*  public void consumeMessage(byte[] messageBody) {
        System.out.println("字节数组方法，消息内容：" + new String(messageBody));
    }*/

    public void consumeMessage(String messageBody) {
        System.out.println("字符串方法，消息内容：" + new String(messageBody));
    }

    public void method1(String messageBody) {
        System.out.println("字符串方法111，消息内容：" + new String(messageBody));
    }

    public void method2(String messageBody) {
        System.out.println("字符串方法222，消息内容：" + new String(messageBody));
    }

    public void consumeMessage(Map messageBody) {
        System.out.println("Map方法，消息内容：" + messageBody);
    }

    public void consumeMessage(Order order) {
        try {
            System.out.println("Order方法，消息内容：" + new ObjectMapper().writeValueAsString(order));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void consumeMessage(Packaged packaged) {
        try {
            System.out.println("Packaged方法，消息内容：" + new ObjectMapper().writeValueAsString(packaged));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void consumeMessage(File messageBody) {
        System.out.println("File方法，消息内容：" + messageBody);
    }
}
