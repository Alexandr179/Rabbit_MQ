package ru.coach.mq.rabbit.example_4;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ImagesProducer {
    // очередь для png
    public static final String PNG_QUEUE = "images_png_queue";
    // очередь для jpg
    public static final String JPG_QUEUE = "images_jpg_queue";
    //роутинг по PNG
    public static final String PNG_ROUTING_KEY = "png";
    //роутинг по PNG
    public static final String JPG_ROUTING_KEY = "jpg";

    public static final String IMAGES_EXCHANGE = "images_direct_exchange";

    public static final String EXCHANGE_TYPE = "direct";


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(IMAGES_EXCHANGE, EXCHANGE_TYPE);// create Exchange
            channel.queueBind(PNG_QUEUE, IMAGES_EXCHANGE, PNG_ROUTING_KEY);// определяем очередь по ключу !
            channel.queueBind(JPG_QUEUE, IMAGES_EXCHANGE, JPG_ROUTING_KEY);

            File file = new File("images.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String currentImagesURL;
            while ((currentImagesURL = reader.readLine()) != null){// ключ  парсится из ..currentImagesURL

                // в зависимости от типа файла отправляем его в конкретную очередь
                String currentRouting = currentImagesURL.substring(currentImagesURL.lastIndexOf(".") + 1);
                if(currentRouting.equals("jpeg")){
                    currentRouting = "jpg";
                }

                // >>> распределение задач по ключу...
                channel.basicPublish(IMAGES_EXCHANGE, currentRouting, null, currentImagesURL.getBytes());
            }

        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
