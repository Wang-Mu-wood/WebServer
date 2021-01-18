package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
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
    //保存uri中的请求部分(?左侧内容)
    private String requestURI;
    //保存uri中的参数部分(?右侧内容)
    private String queryString;
    //保存每一组参数，key为参数名，value为参数值
    private Map<String,String> parameters = new HashMap<>();

    //消息头相关信息
    private Map<String,String> headers = new HashMap<>();
    //和连接相关的属性
    private Socket socket;

    /**
     * 构造方法,在实例化HTTPRequest的同时完成了解析请求的工作
     * @param socket
     */
    public HttpRequest(Socket socket) throws EmptyRequestException {
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
    private void parseRequestLine() throws EmptyRequestException {
        System.out.println("HttpRequest:解析请求行...");
        try{
            String line = readLine();
            //如果读取请求行内容返回为空串，说明本次为空请求
            if (line.isEmpty()){
                throw new EmptyRequestException();
            }
            System.out.print("请求行:"+line);
            //将请求行按照空格拆分为三部分，并赋值给上述三个变量
            String[] data =line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol= data[2];

            //进一步解析uri
            parseUri();

            System.out.println("method:"+method);
            System.out.println("uri:"+uri);
            System.out.println("protocol:"+protocol);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("HttpRequest:请求解析完毕！");
    }

    /**
     * 进一步对uri进行拆分解析，因为uri可能包含参数
     */
    private void parseUri(){
        System.out.println("HttpRequest:进一步解析uri...");
        /*
            对抽象路径解码(解决传递中文问题，将%xx内容还原对应的文字)
         */
        try {
            /*
                URLDecoder提供的静态方法:
                static String decode (String str , String csn)
                将给定的字符串(第一个参数)中%XX这样的内容按照给定的字符集(第二个参数)还原为
                对应的文字并替换原有的%XX部分。将替换后的字符串返回
                例如:
                uri:
                /myweb/regUser?username=%E8%80%83%E4%BC%A0%E5%A5%87&password=&nickname=chuanqi&age=22
                经过下面代码处理后，decode方法返回的字符串为:
                /myweb/regUser?username=范传奇&password=&nickname=chuanqi&age=22

             */
            uri = URLDecoder.decode(uri,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*
            uri可能存在两种情况:含有参数，不含有参数
            含有参数(uri中包含"?"):
            首先按照"?"将uri拆分为两部分，第一部分赋值给requestURI这个属性。第二部分赋值给
            queryString这个属性。
            然后在将queryString(uri中的参数部分)进行进一步拆分，来得到每一组参数。
            首先将queryString按照"&"拆分出每一组参数，然后每组参数在按照"="拆分为参数名与参数值
            之后将参数名作为key，参数值作为value保存到parameters这个Map中保存即可。

            不含有参数(uri中不包含"?")
            则直接将uri的值赋值给requestURI即可。
         */
        if (uri.contains("?")){
            //按?拆分请求与参数部分
            String[] data = uri.split("\\?");
            requestURI = data[0];
            if (data.length>1){//确定是否参数部分
                queryString = data[1];
                //拆分出每一组参数
                data = queryString.split("&");
                //遍历每组参数再进行拆分
                for (String para:data) {
                    String[] paras = para.split("=");
                    if (paras.length>1){
                        parameters.put(paras[0],paras[1]);
                    }else{
                        parameters.put(paras[0],null);
                    }
                }
            }
            }else{
            requestURI = uri;
        }
        System.out.println("requestURI:"+requestURI);
        System.out.println("queryString:"+queryString);
        System.out.println("parameters:"+parameters);
        System.out.println("HttpRequest:进一步解析uri完毕！");
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

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
}
