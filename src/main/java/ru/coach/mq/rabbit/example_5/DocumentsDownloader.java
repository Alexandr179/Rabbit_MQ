package ru.coach.mq.rabbit.example_5;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class DocumentsDownloader {

    public static final String DOCUMENTS_ROUTING_KEY = "files.documents.*";
    public static final String FILES_EXCHANGE = "files_topic_exchange";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);

            //     Consumer САМ! для своих сообщений создает очередь...
            String queueName = channel.queueDeclare().getQueue();
            //     привязываем к EXCHANGE по ключу  "files.documents"
            channel.queueBind(queueName, FILES_EXCHANGE, DOCUMENTS_ROUTING_KEY);

            channel.basicConsume(queueName, false, ((consumerTag, message) -> {
                String imageUrl = new String(message.getBody());
                System.out.println("Start any File load: " + imageUrl);
                URL url = new URL(imageUrl);
                String fileName = url.getFile();
                try {
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());// java.nio **оч) удобна для скачивания по URL !

                    File output = new File("downloaded/documents/" + UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf(".")));
                    FileOutputStream fileOutputStream = new FileOutputStream(output);
                    fileOutputStream.getChannel()
                            .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                    System.out.println("Finish load any File: " + imageUrl);

                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);// ack! ..acknowledge become's message
                } catch (IOException e){
                    System.err.println("FAILED");
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);// false -> delete message from Queue!
                }
            }), consumerTag -> {});

        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException(e);
        }
    }
}
