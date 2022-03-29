package com.bowen.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket();

        // 绑定 port
        serverSocket.bind(new InetSocketAddress("127.0.0.1", port));

        // Listener 阻塞
        Socket socket = serverSocket.accept();
        System.out.println(socket);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String str = bufferedReader.readLine();
            if (str == null || str.isEmpty()) {
                break;
            }

            stringBuilder.append(str);
            stringBuilder.append("\r\n");
        }

        System.out.println(stringBuilder);

        // HTTP response header
        socket.getOutputStream().write("HTTP/1.1 200 OK\r\n".getBytes());
        socket.getOutputStream().write("content-type: text/html\r\n".getBytes());
//        socket.getOutputStream().write("content-type: text/plain\r\n".getBytes());
//        socket.getOutputStream().write("content-disposition: attachment;filename=123.txt\r\n".getBytes());

        // HTTP response header/body 分割符
        socket.getOutputStream().write("\r\n".getBytes());

        // HTTP response body
        socket.getOutputStream().write(("<html><h1>Hello</h1><p>World!</p>" + Instant.now() + "</html>").getBytes());

        // submit
        socket.getOutputStream().flush();
    }
}
