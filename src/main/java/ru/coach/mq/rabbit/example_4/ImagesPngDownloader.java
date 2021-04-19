package ru.coach.mq.rabbit.example_4;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ImagesPngDownloader {
    public static final String PNG_QUEUE = "images_png_queue";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try{
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);
            channel.basicConsume(PNG_QUEUE, false, ((consumerTag, message) -> {// autoAck=false,  ...channel.basicAck ниже самостоятельно удаляем mess из очереди
                String imageUrl = new String(message.getBody());
                System.out.println("Start image load: " + imageUrl);
                URL url = new URL(imageUrl);
                String fileName = url.getFile();
                try {
                    // * ... как правило выкидывает здесь, если изображение защищено от копирования
                    BufferedImage image = ImageIO.read(url);// load Image !!

                    // convert PNG -> JPG с заменой альфа-канала розовым цветом
                    BufferedImage convertedImage = new BufferedImage(
                            image.getWidth(),
                            image.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    convertedImage.createGraphics().drawImage(image, 0, 0, Color.PINK, null);


                    File output = new File("downloaded/" + UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf(".")));// write imageFile to dir..
                    ImageIO.write(image, "png", output);
                    System.out.println("Finish load image " + imageUrl);

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
