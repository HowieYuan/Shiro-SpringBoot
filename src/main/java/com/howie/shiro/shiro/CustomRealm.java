package com.howie.shiro.shiro;

import com.howie.shiro.mapper.UserMapper;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description 自定义 Realm
 * @Date 2018-03-25
 * @Time 21:46
 */
@Configuration
public class CustomRealm extends AuthorizingRealm {
    private final UserMapper userMapper;

    @Autowired
    public CustomRealm(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取身份验证信息
     * @param authenticationToken 用户身份信息 token
     * @return
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 第一步从 token 中取出身份信息
        String username = (String) authenticationToken.getPrincipal();
        // 第二步：根据用户输入的userCode从数据库查询,如果查询不到返回null
        String password = userMapper.getPassword(username);
        if (password == null) {
            return null;
        }
        // 如果查询到返回认证信息AuthenticationInfo
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
                username, password, this.getName());
        return simpleAuthenticationInfo;
    }

    /**
     * 获取授权信息
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }
}
