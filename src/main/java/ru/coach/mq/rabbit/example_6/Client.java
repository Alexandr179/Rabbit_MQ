package ru.coach.mq.rabbit.example_6;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Client {

    private Channel channel;
    // очередь, в которую будут поступать ответы от сервера
    private String replyQueueName;
    // очередь, в которую поступают задачи от клиента (каждый клиент делает очередь для себя..)
    public static final String ReQUEST_QUEUE_NAME = "calls";

    public Client(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            replyQueueName = channel.queueDeclare("", false, true, false, null).getQueue();
        } catch (IOException | TimeoutException e){
            throw new IllegalArgumentException();
        }
    }


    public Long downloadFileAndGetSize(String fileUrl) {

        String correlationId = UUID.randomUUID().toString();
        try {
            // формируем свойства, которые мы направим вместе с задачей
            BasicProperties properties = new BasicProperties.Builder()
                    .correlationId(correlationId)// id
                    .replyTo(replyQueueName)//  очередь в которую нужно отвечать
                    .build();

            // ! ПУБЛИКУЕМ ЗАДАЧУ
            channel.basicPublish("", ReQUEST_QUEUE_NAME, properties, fileUrl.getBytes());
            // класс, позволяющий отложить задачу в поток... и теперь - задачи по вызову метода приходят не в порядке очередности, а по мере выполнения
            CompletableFuture<Long> result = new CompletableFuture<>();

            String consumerTag = channel.basicConsume(replyQueueName, true, (tag, message) -> {
                // смотрим - этот ответ был конкретно по этой задаче? <-----
                if(message.getProperties().getCorrelationId().equals(correlationId)) {
                    //  ..закладываем результат (размер файла в result) и идем дальше
                    result.complete(Long.parseLong(new String(message.getBody())));// -завершает поток
                }
            }, tag -> {});
            // !! вы не получите размер файла, пока у result не будет вызван complete (многопоточности)
            Long value = result.get();// !! выводит поток в ожидание, до команды .complete ***** (можно посмотреть исходники и ужаснуться)
            channel.basicCancel(consumerTag);
            return value;

        } catch (IOException | InterruptedException | ExecutionException e){
            throw new IllegalArgumentException(e);
        }
    }
}
