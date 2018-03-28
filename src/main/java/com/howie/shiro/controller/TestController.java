package com.howie.shiro.controller;

import com.howie.shiro.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description
 * @Date 2018-03-25
 * @Time 22:17
 */
@RestController
public class TestController {
    private final UserMapper userMapper;

    @Autowired
    public TestController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @RequestMapping(value="/login",method= RequestMethod.GET)
    public String submitLogin() {
        try {

            UsernamePasswordToken token = new UsernamePasswordToken("howie",
                    "123456");
            SecurityUtils.getSubject().login(token);
            return "成功";
        } catch (Exception ignored) {
            return "失败";
        }
    }
}
