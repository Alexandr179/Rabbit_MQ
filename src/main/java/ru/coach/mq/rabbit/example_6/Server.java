package ru.coach.mq.rabbit.example_6;

import com.rabbitmq.client.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class Server {
    public static final String ReQUEST_QUEUE_NAME = "calls";

    public Server() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // создаем очередь для задач
            channel.queueDeclare(ReQUEST_QUEUE_NAME, false, false, false, null);
            // очистка очереди
            channel.queuePurge(ReQUEST_QUEUE_NAME);
            // сервер может обрабатывать только одну задачу
            channel.basicQos(1);

            // монитор
            Object monitor = new Object();

            // реакция на получение задачи от клиента:
            DeliverCallback deliverCallback = (consumerTag, message) -> {
                // replyProperties - свойства которые мы будем отправлять в ответе
                BasicProperties replyProperties = new BasicProperties
                        .Builder()
                        .correlationId(message.getProperties().getCorrelationId())
                        .build();

                // забираем имя файла из сообщения клиента..
                String fileUrl = new String(message.getBody());
                // скачиваем файл и  получаем его размер
                Long fileSize = downloadFileAndGetSize(fileUrl);
                // публикуем ответ в очередь  --->>  replyTo
                channel.basicPublish("", message.getProperties().getReplyTo(), replyProperties,
                        fileSize.toString().getBytes());
                // говорим, что все прошло успешно
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);

                // оповещаем поток, который ждет на мониторе
                synchronized (monitor){
                    monitor.notify();
                }
            };

            // запускаем механизм реакции на запросы клиента
            channel.basicConsume(ReQUEST_QUEUE_NAME, false, deliverCallback, consumerTag -> {});

            while (true){
                synchronized (monitor){
                    try {
                        monitor.wait();
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }

        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }


    // закачка файла, выдача размера
    private Long downloadFileAndGetSize(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            long size = urlConnection.getContentLength();
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            File output = new File("downloaded/" + UUID.randomUUID().toString() + fileUrl.substring(fileUrl.lastIndexOf(".")));
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            return size;

        } catch (IOException e) {
            new IllegalArgumentException(e);
        }
        return null;
    }


}
