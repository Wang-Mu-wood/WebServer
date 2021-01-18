package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示HTTP的一个请求
 * 每个请求由三个部分构成:请求行，消息头，消息正文
 */
public class HttpRequest {
    //请求行相关信息
    //请求行中的请求方式
    private String method;
    //抽象路径部分
    private String uri;
    //协议版本
    private String protocol;

    //消息头相关信息
    private Map<String,String> headers = new HashMap<>();
    //和连接相关的属性
    private Socket socket;

    /**
     * 构造方法,在实例化HTTPRequest的同时完成了解析请求的工作
     * @param socket
     */
    public HttpRequest(Socket socket) {
        this.socket = socket;
        /*
            1:解析请求行
            2:解析消息头
            3:解析消息正文
         */
        //条理性更好
        parseRequestLine();
        parseHeaders();
        parseContent();
    }
    private void parseRequestLine() {
        System.out.println("HttpRequest:解析请求行...");
        try{
            String line = readLine();
            System.out.print("请求行:"+line);
            //将请求行按照空格拆分为三部分，并赋值给上述三个变量
            String[] data =line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol= data[2];
            System.out.println("method:"+method);
            System.out.println("uri:"+uri);
            System.out.println("protocol:"+protocol);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("HttpRequest:请求解析完毕！");
    }
    private void parseHeaders() {
        System.out.println("HttpRequest:解析消息头...");
        try{
            while (true) {
                String line = readLine();
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
            System.out.println("headers:"+headers);//保留打桩让后期方便找bug
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("HttpRequest:消息头解析完毕！");
    }
    private void parseContent(){
        System.out.println("HttpRequest:解析消息正文...");
        System.out.println("HttpRequest:消息正文解析完毕！");
    }
        //消息正文相关信息

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

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

//    public Map<String, String> getHeaders() {
//        return headers;//为了保证封装性所以不直接返回
//    }
    public String getHeader(String name) {
        return headers.get(name);//获取key的值
    }
}
