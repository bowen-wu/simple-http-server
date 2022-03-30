package com.bowen.http;

import javax.annotation.Resources;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleHttpServer {
    public static Pattern RESOURCE_PATTERN = Pattern.compile("/.*\\s");

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket();

        // 绑定 port
        serverSocket.bind(new InetSocketAddress("127.0.0.1", port));

        while (true) {
            // Listener 阻塞
            Socket socket = serverSocket.accept();
            System.out.println(socket);

            // 获取报文信息
            List<String> messageList = getMessageList(socket);

            String resourceName = getMatcherStr(messageList.get(0));

            // HTTP response header
            socket.getOutputStream().write("HTTP/1.1 200 OK\r\n".getBytes());

            if (isFile(resourceName, ".js") || isFile(resourceName, ".css") || isFile(resourceName, ".jpeg")) {
                setHeader(socket, resourceName);

                // HTTP response header/body 分割符
                socket.getOutputStream().write("\r\n".getBytes());

                InputStream inputStream = Resources.class.getResourceAsStream(resourceName);
                assert inputStream != null;
                writeOutputStream(socket, resourceName, inputStream);

                // submit
                socket.getOutputStream().flush();
            } else {
                socket.getOutputStream().write("content-type: text/html\r\n".getBytes());
//        socket.getOutputStream().write("content-type: text/plain\r\n".getBytes());
//        socket.getOutputStream().write("content-disposition: attachment;filename=123.txt\r\n".getBytes());

                // HTTP response header/body 分割符
                socket.getOutputStream().write("\r\n".getBytes());

                // HTTP response body
                socket.getOutputStream().write(("<html>" + "<script src='/myScript.js'></script>" + "<link href='style.css' rel='stylesheet'>" + "<h1>Hello</h1>" + "<p>World!</p>" + "<div>Text</div>" + "<img src='image.jpeg'/>" + "<div class='text'>Text with class</div>" + Instant.now() + "</html>").getBytes());

                // submit
                socket.getOutputStream().flush();
            }

            socket.close();
        }
    }

    private static void writeOutputStream(Socket socket, String resourceName, InputStream inputStream) throws IOException {
        if (isFile(resourceName, ".js") || isFile(resourceName, ".css")) {
            BufferedReader file = new BufferedReader(new InputStreamReader(inputStream));
            String fileLine;
            while ((fileLine = file.readLine()) != null) {
                socket.getOutputStream().write(fileLine.getBytes());
            }
        } else if (isFile(resourceName, ".jpeg")) {
            int b;
            while ((b = inputStream.read()) != -1) {
                socket.getOutputStream().write(b);
            }
        }
    }

    private static boolean isFile(String resourceName, String suffix) {
        return resourceName.endsWith(suffix);
    }

    private static List<String> getMessageList(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        List<String> messageList = new ArrayList<>();
        String str;
        while (!(str = bufferedReader.readLine()).isEmpty()) {
            System.out.println(str);
            messageList.add(str);
        }
        return messageList;
    }

    private static void setHeader(Socket socket, String resourceName) throws IOException {
        if (isFile(resourceName, ".js")) {
            socket.getOutputStream().write("content-type: text/javascript\r\n".getBytes());
        } else if (isFile(resourceName, ".css")) {
            socket.getOutputStream().write("content-type: text/css\r\n".getBytes());
        } else if (isFile(resourceName, ".jpeg")) {
            socket.getOutputStream().write("content-type: image/jpeg\r\n".getBytes());
        }
    }

    public static String getMatcherStr(String target) {
        Matcher matcher = RESOURCE_PATTERN.matcher(target);
        while (matcher.find()) {
            String str = matcher.group(0);
            return str.substring(0, str.length() - 1);
        }
        return "";
    }
}
