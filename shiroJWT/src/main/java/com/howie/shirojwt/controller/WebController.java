package com.howie.shirojwt.controller;

import com.howie.shirojwt.mapper.UserMapper;
import com.howie.shirojwt.model.ResultMap;
import com.howie.shirojwt.util.JWTUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description
 * @Date 2018-04-09
 * @Time 17:12
 */
@RestController
public class WebController {
    private final UserMapper userMapper;
    private final ResultMap resultMap;

    @Autowired
    public WebController(UserMapper userMapper, ResultMap resultMap) {
        this.userMapper = userMapper;
        this.resultMap = resultMap;
    }

    @PostMapping("/login")
    public ResultMap login(@RequestParam("username") String username,
                           @RequestParam("password") String password) {
        String realPassword = userMapper.getPassword(username);
        if (realPassword == null) {
            return resultMap.fail().code(401).message("用户名错误");
        } else if (realPassword.equals(password)) {
            return resultMap.fail().code(401).message("密码错误");
        } else {
            return resultMap.success().code(200).message(JWTUtil.createToken(username, password));
        }
    }

    @GetMapping("/article")
    public ResultMap article() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return resultMap.success().code(200).message("You are already logged in");
        } else {
            return resultMap.success().code(200).message("You are guest");
        }
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public ResultMap requireAuth() {
        return resultMap.success().code(200).message("You are authenticated");
    }

    @GetMapping("/require_role")
    @RequiresRoles("admin")
    public ResultMap requireRole() {
        return resultMap.success().code(200).message("You are visiting require_role");
    }

    @GetMapping("/require_permission")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    public ResultMap requirePermission() {
        return resultMap.success().code(200).message("You are visiting permission require edit,view");
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultMap unauthorized() {
        return resultMap.success().code(401).message("token 认证失败");
    }
}
