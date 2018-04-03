package com.howie.shiro;

import com.howie.shiro.config.ShiroConfig;
import com.howie.shiro.mapper.UserMapper;
import com.howie.shiro.shiro.CustomRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description
 * @Date 2018-03-25
 * @Time 21:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginTest {
    @Autowired
    public ShiroConfig shiroConfig;

    /**
     * 用户登陆和退出
     */
    @Test
    public void testLoginAndLogout() {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        // 这里的账号和密码 将来是由用户输入进去
        UsernamePasswordToken token = new UsernamePasswordToken("howie",
                "123456");
        // 执行认证提交
        subject.login(token);
        // 是否认证通过
        boolean isAuthenticated = subject.isAuthenticated();
        System.out.println("是否认证通过：" + isAuthenticated);
        // 退出操作
        subject.logout();
        System.out.println("登陆已经注销");
        // 是否认证通过
        isAuthenticated = subject.isAuthenticated();
        System.out.println("是否认证通过：" + isAuthenticated);
    }
}
