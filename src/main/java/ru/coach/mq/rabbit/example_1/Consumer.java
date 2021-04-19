package ru.coach.mq.rabbit.example_1;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

// read messages from Queue's
public class Consumer {

    public static final String QUEUE_1 = "queue_1";
    public static final String QUEUE_2 = "queue_2";
    public static final String QUEUE_3 = "queue_3";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");// by default port is 5672

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            channel.basicConsume(QUEUE_1, true, ((consumerTag, message) -> {// next... to Queue_2 (anf -> 'true' autoAck)
                System.out.println(consumerTag);
                System.out.println("Message from: " + QUEUE_1 + " " + new String(message.getBody()));// next... to Queue_2
            }), consumerTag -> {});

        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
