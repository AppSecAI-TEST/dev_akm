package com.zongsheng.drink.h17.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.zongsheng.drink.h17.MyApplication;
import com.zongsheng.drink.h17.common.L;
import com.zongsheng.drink.h17.common.SysConfig;


import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Created by Suchengjian on 2017.2.13.
 */

public class ClientConnectMQ {

    private static volatile ClientConnectMQ instance = null;

    /**send*/
    private final ConnectionFactory factory = new ConnectionFactory();
    /**reciever*/
    private final ConnectionFactory quefactory = new ConnectionFactory();
        private Connection connection = null;
    private Channel channel;

    private ClientConnectMQ() {
        factory.setHost(SysConfig.MQ_HOST);
        factory.setPort(SysConfig.MQ_PORT);
        factory.setUsername(SysConfig.MQ_USERNAME);
        factory.setPassword(SysConfig.MQ_PASSWORLD);
        factory.setAutomaticRecoveryEnabled(true);
    }

    public static ClientConnectMQ getInstance() {
        if (instance == null) {
            synchronized (ClientConnectMQ.class) {
                if (instance == null) {
                    instance = new ClientConnectMQ();
                }
            }
        }
        return instance;
    }

    ConnectionFactory getQuefactory(){
        return quefactory;
    }

    void setfactoryHost(String mq_host){
//        quefactory.setHost(mq_host);
        quefactory.setHost("118.178.139.93");
        quefactory.setPort(SysConfig.MQ_PORT);
        quefactory.setUsername(SysConfig.MQ_USERNAME);
        quefactory.setPassword(SysConfig.MQ_PASSWORLD);
        quefactory.setAutomaticRecoveryEnabled(true);
    }

   boolean sendMessage(String msg , String queName) {
        L.v(SysConfig.ZPush, "SendMsg ZPushService 连接设置--->" + factory.hashCode() + "---" +queName);
        try {
            L.v(SysConfig.ZPush, "sendMsgsss0........" + msg);
            //创建一个连接
            Connection conn = factory.newConnection();
            //创建一个渠道
            Channel channel2 = conn.createChannel();
            //为channel定义queue的属性，queueName为Queue名称
            channel2.queueDeclare(queName, true, false, false, null);
            channel2.basicPublish("", queName, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            channel2.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendShipStatus2MQ(final String msg){
//        L.v(SysConfig.ZPush, "sendShipStatus2MQ ZPushService 连接设置--->" + factory.hashCode());

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(msg,SysConfig.MQ_SHIPTODO);
            }
        }).start();
    }

    void receiverMsg2MQ(Context context, final Handler handler) {
//        L.v(SysConfig.ZPush, "receiverMsg2MQ ZPushService 连接设置--->" + quefactory.hashCode());
        try {
            //使用之前的设置，建立连接
            if (connection == null || !connection.isOpen()) {
                connection = quefactory.newConnection();
            }
            //创建一个通道
            if (channel == null || !channel.isOpen()) {
                channel = connection.createChannel();
            }
            //3.创建队列消费者
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume("Shipment_" + MyApplication.getInstance().getMachine_sn(), false, consumer);//指定消费队列
            channel.basicQos(0, 1, false);
            L.v(SysConfig.ZPush, "Waiting for messages2……");
            while (true) {
                //4.开启nextDelivery阻塞方法（内部实现其实是阻塞队列的take方法）
                L.v(SysConfig.ZPush, "Waiting for messages2.5 ……");
                MyApplication.getInstance().setMQstate(true);
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                L.v(SysConfig.ZPush, "[Received]" + message);

                Intent intent2 = new Intent();
                intent2.setAction(SysConfig.PUSH_MESSAGE_ACTION);
                intent2.putExtra("data", message); //这个data为你要传的数据
                intent2.putExtra("type", "2");
                context.sendBroadcast(intent2);
            }
        } catch (Exception e1) {
            MyApplication.getInstance().setMQstate(false);
            createChannelAgain();
            e1.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (channel != null) {
                    channel.close();
                }
                MyApplication.getInstance().setMQstate(false);
            } catch (TimeoutException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createChannelAgain() {
        try {
            Connection connection = quefactory.newConnection();
            //创建一个通道
            Channel channel = connection.createChannel();
            //3.创建队列消费者
            channel.queueDeclare("Shipment_" + MyApplication.getInstance().getMachine_sn(), true, false, false, null);
            channel.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
