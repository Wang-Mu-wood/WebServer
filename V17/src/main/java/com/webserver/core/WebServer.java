package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer主类
 */
public class WebServer {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    /**
     * 构造器，用于初始化
     */
    public WebServer(){
        try {
            System.out.println("正在启动服务端...");
            serverSocket = new ServerSocket(8088);
            threadPool = Executors.newFixedThreadPool(50);
            System.out.println("服务端启动完毕!");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 服务端开始工作的方法
     */
    public void start(){
        try {

            while (true) {
                System.out.println("等待客户端链接...");
                Socket socket = serverSocket.accept();
                System.out.println("一个客户端链接了！");

                //启动一个线程来处理客户端的交互工作
                ClientHandler handler = new ClientHandler(socket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }
}
