package ru.coach.mq.rabbit.example_5;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class FilesProducer {

    public static final String PNG_ROUTING_KEY = "files.images.png";
    public static final String JPG_ROUTING_KEY = "files.images.jpg";
    public static final String PDF_ROUTING_KEY = "files.documents.pdf";
    public static final String HTML_ROUTING_KEY = "files.documents.html";

    // создаем в GUI FILES_EXCHANGE = "files_topic_exchange", как transient, topic !
    public static final String FILES_EXCHANGE = "files_topic_exchange";
    public static final String EXCHANGE_TYPE = "topic";


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(FILES_EXCHANGE, EXCHANGE_TYPE);// create Exchange

            System.out.println("Tap anything to start FilesProducer:)");
            Scanner scanner = new Scanner(System.in);//                      -> tap in console .."Go" to starting :)))))
            scanner.nextLine();



            File file = new File("files.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String currentFile;
            while ((currentFile = reader.readLine()) != null) {
                String currentRouting = currentFile.substring(currentFile.lastIndexOf("."));// Внимание! example отличается ".jpg"

                if (currentRouting.equals(".jpeg")) {
                    currentRouting = ".jpg";
                }

                switch (currentRouting) {
                    case ".jpg":
                        currentRouting = JPG_ROUTING_KEY;
                        break;
                    case ".png":
                        currentRouting = PNG_ROUTING_KEY;
                        break;
                    case ".html":
                        currentRouting = HTML_ROUTING_KEY;
                        break;
                    default:
                        currentRouting = PDF_ROUTING_KEY;
                        break;
                }
                // на основании расширения файла - подбираем routing_key *
                channel.basicPublish(FILES_EXCHANGE, currentRouting, null, currentFile.getBytes());
            }

        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
