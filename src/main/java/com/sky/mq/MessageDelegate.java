package com.sky.mq;

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
}
