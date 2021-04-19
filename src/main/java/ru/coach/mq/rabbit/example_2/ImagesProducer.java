package ru.coach.mq.rabbit.example_2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ImagesProducer {
    public static final String IMAGES_QUEUE = "images_queue";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            File file = new File("images.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentImagesURL;
            while ((currentImagesURL = reader.readLine()) != null){
                    channel.basicPublish("", IMAGES_QUEUE, null, currentImagesURL.getBytes());
            }
        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
