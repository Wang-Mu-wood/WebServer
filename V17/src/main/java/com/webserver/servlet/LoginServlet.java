package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 处理登录业务
 */
public class LoginServlet extends HttpServlet{
    public void service(HttpRequest request, HttpResponse response){
        System.out.println("LoginServlet:开始处理登录...");
        //获取登录信息
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //处理登录
        //判断用户名和密码不为null
        if (username==null||password==null){
            response.setEntity(new File("./webapps/myweb/login_fail.html"));
            return;
        }
        try(
                RandomAccessFile raf = new RandomAccessFile("user.dat","r");
                ) {
            byte[] data = new byte[32];
            for (int i = 0; i < raf.length()/100; i++) {
                raf.seek(i*100);
                raf.read(data);
                String name = new String(data,"UTF-8").trim();
                //判断用户名和密码是否正确
                if (name.equals(username)){
                    raf.read(data);
                    String pw = new String(data,"utf-8").trim();
                    if (pw.equals(password)) {
                        response.setEntity(new File("./webapps/myweb/login_success.html"));
                        return;
                    }
                    //执行到这里说明了用户名对了，但是密码不对
                    break;//停止读取工作，因为没有重复记录
                }
                response.setEntity(new File("./webapps/myweb/login_fail.html"));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        response.setEntity(new File("./webapps/login_fail.html"));
        System.out.println("LoginServlet:登录处理完毕！");
    }
}
