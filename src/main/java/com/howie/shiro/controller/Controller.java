package com.howie.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description
 * @Date 2018-04-03
 * @Time 22:28
 */
@RestController
public class Controller {
    @RequestMapping(value = "/login/{username}/{password}", method = RequestMethod.GET)
    public String submitLogin(@PathVariable("username") String username,
                              @PathVariable("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        // 这里的账号和密码 将来是由用户输入进去
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        // 执行认证提交
        subject.login(token);
        // 是否认证通过
        boolean isAuthenticated = subject.isAuthenticated();
//        System.out.println("是否认证通过：" + isAuthenticated);
//        // 退出操作
//        subject.logout();
//        System.out.println("登陆已经注销");
//        // 是否认证通过
//        isAuthenticated = subject.isAuthenticated();
//        System.out.println("是否认证通过：" + isAuthenticated);
        return "是否认证通过：" + isAuthenticated;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String submitLogin() {
        return "test";
    }
}
