package com.bowen.http;

import javax.annotation.Resources;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            List<String> messageList = new ArrayList<>();
            String str;
            while (!(str = bufferedReader.readLine()).isEmpty()) {
                System.out.println(str);
                messageList.add(str);
            }

            String resourceName = getMatcherStr(messageList.get(0));

            // HTTP response header
            socket.getOutputStream().write("HTTP/1.1 200 OK\r\n".getBytes());

            if (resourceName.endsWith(".js") || resourceName.endsWith(".css") || resourceName.endsWith(".jpeg")) {
                if (resourceName.endsWith(".js")) {
                    socket.getOutputStream().write("content-type: text/javascript\r\n".getBytes());
                } else if (resourceName.endsWith(".css")) {
                    socket.getOutputStream().write("content-type: text/css\r\n".getBytes());
                } else if (resourceName.endsWith(".jpeg")) {
                    socket.getOutputStream().write("content-type: image/jpeg\r\n".getBytes());
                }

                // HTTP response header/body 分割符
                socket.getOutputStream().write("\r\n".getBytes());

                InputStream inputStream = Resources.class.getResourceAsStream(resourceName);
                assert inputStream != null;
                if (resourceName.endsWith(".js") || resourceName.endsWith(".css")) {
                    BufferedReader file = new BufferedReader(new InputStreamReader(inputStream));
                    String fileLine;
                    while ((fileLine = file.readLine()) != null) {
                        socket.getOutputStream().write(fileLine.getBytes());
                    }
                } else if (resourceName.endsWith(".jpeg")) {
                    int b;
                    while ((b = inputStream.read()) != -1) {
                        socket.getOutputStream().write(b);
                    }
                }

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

    public static String getMatcherStr(String target) {
        Matcher matcher = RESOURCE_PATTERN.matcher(target);
        while (matcher.find()) {
            String str = matcher.group(0);
            return str.substring(0, str.length() - 1);
        }
        return "";
    }
}
