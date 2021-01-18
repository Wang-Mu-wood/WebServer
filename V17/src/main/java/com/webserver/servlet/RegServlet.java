package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 处理用户注册业务
 */
public class RegServlet extends HttpServlet{
    public void service(HttpRequest request, HttpResponse response){
        System.out.println("RegServlet:开始处理注册...");
        //1获取用户在注册页面上输入的注册信息
        //这里getParameter方法传入的参数值应当与注册页面表单中输入框中name属性指定的值一致!!
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");
        System.out.println(username+","+password+","+nickname+","+ageStr);

        /*
            添加对用户输入的信息的验证工作
            要求:
            如果用户名，密码，昵称和年龄为null，或者年龄输入的不是一个数字(正则表达式验证)
            则直接设置response响应一个注册失败的提示页面。
            该页面方法在webapps/myweb目录下，名字为reg_input_error.html
            页面居中显示一行字:注册信息输入有误，请重新注册
            然后加一个超链接返回到注册页面

            只有验证通过了，才能进行下面的注册操作!
         */
        if (username==null||password==null||nickname==null||ageStr==null||!ageStr.matches("[0-9]+")){
            File file =new File("./webapps/myweb/reg_input_error.html");
            response.setEntity(file);
            return;
        }

        int age = Integer.parseInt(ageStr);//将年龄转换为int值
        //2将该用户信息写入user.dat文件保存
        try(
                RandomAccessFile raf = new RandomAccessFile("user.dat","rw");
         ){
            /*
                先读取user.dat文件中现有的所有数据，将每条记录的用户名服务出来并与当前注册用户的
                用户名对比，如果已存在，则直接响应页面:have_user.html
                页面居中显示一行字:注册信息已存在，请重新注册
                然后加一个超链接返回到注册页面

                否则猜执行下面原有的注册操作
             */
            for (int i = 0; i < raf.length()/100; i++) {
                raf.seek(i * 100);//将指针移动到开始的位置
                byte[] data = new byte[32];
                raf.read(data);
                String name = new String(data, "UTF-8").trim();
                if (name.equals(username)) {
                    //如果是重复用户则跳转页面
                    File file = new File("./webapps/myweb/have_user.html");
                    response.setEntity(file);
                    return;//结束程序运行
                }
            }
                raf.seek(raf.length());//先将指针移动到末尾，以便追加记录
                byte[] data = username.getBytes("UTF-8");
                data = Arrays.copyOf(data, 32);
                raf.write(data);

                data = password.getBytes("UTF-8");
                data = Arrays.copyOf(data, 32);
                raf.write(data);

                data = nickname.getBytes("UTF-8");
                data = Arrays.copyOf(data, 32);
                raf.write(data);

                raf.writeInt(age);
                System.out.println("注册完毕！");

                //3响应用户注册成功页面设置到response的正文上
                File file = new File("./webapps/myweb/reg_success.html");
                response.setEntity(file);

        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("RegServlet:处理注册完毕！");
    }
}
