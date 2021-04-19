package ru.coach.mq.rabbit.example_3;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ImagesProducer {
    public static final String EXCHANGE_NAME = "images";
    public static final String EXCHANGE_TYPE = "fanout";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

//          channel.basicPublish(...);
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);// so!.. declare Exchange
            File file = new File("images.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String currentImagesURL;
            while ((currentImagesURL = reader.readLine()) != null){
                channel.basicPublish(EXCHANGE_NAME, "", null, currentImagesURL.getBytes());
            }

        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
