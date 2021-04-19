package ru.coach.mq.rabbit.example_6;


import java.util.Scanner;

public class MainClient {


    public static void main(String[] args) {
        Client client = new Client();

        Scanner scanner = new Scanner(System.in);// передаем с консоли имя файла

        while (true){
            String fileName = scanner.nextLine();
            // вызываем метод клиента, который скачивает файл и возвращает его размер
            System.out.println(client.downloadFileAndGetSize(fileName));
        }
    }
}
