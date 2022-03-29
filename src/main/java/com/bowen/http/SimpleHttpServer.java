package com.bowen.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket();

        // 绑定 port
        serverSocket.bind(new InetSocketAddress("127.0.0.1", port));

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

        // HTTP response header/body 分割符
        socket.getOutputStream().write("\r\n".getBytes());

        // HTTP response body
        socket.getOutputStream().write("Hello!".getBytes());

        // submit
        socket.getOutputStream().flush();
    }
}
