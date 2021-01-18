package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.vo.User;
import org.apache.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成显示所有用户信息的动态页面
 */
public class ShowAllUserServlet extends HttpServlet{
    private static Logger logger =Logger.getLogger(ShowAllUserServlet.class);
    public void service(HttpRequest request, HttpResponse response){
 //       System.out.println("ShowAllUserServlet:开始生成动态页面...");
        logger.info("开始生成动态页面...");//info用来记录普通信息的
        //1从user.dat文件中将所有用户信息读取出来
        List<User> list =new ArrayList<>();
        try(
                RandomAccessFile raf = new RandomAccessFile("user.dat","r")
        ) {
            byte[] data = new byte[32];
            for (int i = 0; i <raf.length()/100 ; i++) {
                raf.read(data);
                String username =new String(data,"UTF-8").trim();
                raf.read(data);
                String nickname =new String(data,"UTF-8").trim();
                raf.read(data);
                String password =new String(data,"UTF-8").trim();
                int age = raf.readInt();
                User user = new User(username,nickname,password,age);
                list.add(user);
            }
            //将user.dat文件用户读取后以User对象现实存入list集合
        }catch (IOException e){
//            e.printStackTrace();
            logger.error(e.getMessage(),e);//error()记录所有错误信息
        }
        for (User user : list) {
            System.out.println(user);
        }

        //2使用thymeleaf将所有用户信息与userlist.html页面结合并生成动态页面

        //2.1:所有要在页面上显示的动态数据都需要存入一个名为Context的类中
        Context context = new Context();
        /*
            向context中添加要在页面中显示的数据，使用类似Map的put方法
            第一个参数为名字，第二个参数为存放的值。将来在也买你上某个位置要显示这些数据时
            会在哪里标注这个名字，thymeleaf就会根据名字获取对应的值来进行显示了
         */
        context.setVariable("users",list);
        //2.2初始化Thymeleaf
        //模板解释器，用来告知引擎模板相关信息
        FileTemplateResolver tr = new FileTemplateResolver();
        tr.setTemplateMode("html");
        tr.setCharacterEncoding("UTF-8");
        //实例化模板引擎
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(tr);
        //2.3将动态数据与模板页面交给引擎来生成动态页面
        /*
            String process(String template,Context context)
            该方法就是thymeleaf将模板页面与context中的数据进行结合来生成一个动态页面。返回值就是
            生成后的动态页面的源代码(html代码)
         */
        String  html =engine.process("./webapps/myweb/userlist.html",context);
        System.out.println(html);
        //3将生成后的动态页面设置到response中发送给客户端
        try {
            byte[] data = html.getBytes("UTF-8");
            //将thymeleaf生成的动态页面代码转换为一组字节设置到response中作为正文数据
            response.setContentData(data);
            response.putHeader("Content-Type","text/html");
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
            logger.error(e.getMessage(),e);//error()记录所有错误信息
        }
 //       System.out.println("ShowAllUserServlet:动态页面生成完毕！");
        logger.info("动态页面生成完毕！");
    }
}
