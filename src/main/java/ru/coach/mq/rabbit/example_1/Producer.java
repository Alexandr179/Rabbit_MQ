package ru.coach.mq.rabbit.example_1;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

// send messages to Queue's
public class Producer {
    public static final String QUEUE_1 = "queue_1";
    public static final String QUEUE_2 = "queue_2";
    public static final String QUEUE_3 = "queue_3";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            channel.basicPublish("", QUEUE_1, null, "Hello send to Queue_1".getBytes());
            channel.basicPublish("", QUEUE_2, null, "Hello send to Queue_2".getBytes());
            channel.basicPublish("", QUEUE_3, null, "Hello send to Queue_3".getBytes());
        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
