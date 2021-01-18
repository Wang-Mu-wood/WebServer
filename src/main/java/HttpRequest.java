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
        this.socket= socket;

        //消息正文相关信息
        try {
            String   line = readLine();
            System.out.print("请求行:"+line);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("headers:"+headers);
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
