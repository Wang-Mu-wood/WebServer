package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 每个客户端连接后都会启动一个线程来完成与该客户端的交互。
 * 交互过程遵循Http协议的一问一答要求，分三步进行处理。
 * 1:解析请求
 * 2:处理请求
 * 3:响应客户端
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    //定义构造器
    public ClientHandler(Socket socket){
        this.socket=socket;
    }
    public void run() {
        try{
            //1:解析请求
            /*
                读取客户端发送过来的第一行字符串(以CRLF结尾)
             */
            String line = readLine();
            System.out.print("请求行:"+line);
            //请求行中的请求方式
            String method;
            //抽象路径部分
            String uri;
            //协议版本
            String protocol;
            //将请求行按照空格拆分为三部分，并赋值给上述三个变量
            String[] data =line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol= data[2];
            System.out.println("method:"+method);
            System.out.println("uri:"+uri);
            System.out.println("protocol:"+protocol);
            //解析消息头
            Map<String,String> headers = new HashMap<>();
            while (true) {
                line = readLine();
                //如果单独读取到的CRLF就应当停止消息头的读取
//                if (" ".equals(line))
//                if (line.length()==0) 这两种都行，但是推荐使用下面这种
                if (line.isEmpty()){//如果是空字符串就停止
                  break;
                }
                System.out.println("消息头:" + line);
                //将消息头按照": "拆分，将名字的值以key，value形式存入headers中
                String[]  arr = line.split(":\\s");
                headers.put(arr[0],arr[1]);
            }
            System.out.println("headers:"+headers);
            //2:处理请求
            //3:响应客户端
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //最终交互完毕后与客户端断开连接
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private  String readLine() throws IOException {
        /*
            同一个socket对象，无论调用多少次getInputStream方法
            获取到的输入流都是同一个
         */
        InputStream in = socket.getInputStream();
        int d;
        //cur表示本次读取的字符，pre表示上次读取的字符
        char cur='a',pre='a';
        //记录读取到的一行字符串
        StringBuilder builder =new StringBuilder();
        while ((d=in.read())!=-1){
            cur = (char)d;
            //如果上次读取的时回车符并且本次读取的时换行符就停止
            if (pre==13 && cur==10){
                break;
            }
            builder.append(cur);
            pre = cur;
        }
        return builder.toString().trim();
    }

}
