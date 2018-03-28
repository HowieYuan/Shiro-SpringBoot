package com.howie.shiro.shiro;

import com.howie.shiro.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description 自定义 Realm
 * @Date 2018-03-25
 * @Time 21:46
 */
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取身份验证信息
     * Shiro中，最终是通过 Realm 来获取应用程序中的用户、角色及权限信息的。
     *
     * @param authenticationToken 用户身份信息 token
     * @return 返回封装了用户信息的 AuthenticationInfo 实例
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 第一步从 token 中取出身份信息
        String username = (String) authenticationToken.getPrincipal();
        // 第二步：根据用户输入的userCode从数据库查询,如果查询不到返回null
        String password = userMapper.getPassword(username);
        // 如果查询到返回认证信息AuthenticationInfo
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
                username, password, this.getName());
        return simpleAuthenticationInfo;
    }

    /**
     * 获取授权信息
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//        System.out.println("权限认证方法：MyShiroRealm.doGetAuthenticationInfo()");
//        SysUser token = (SysUser) SecurityUtils.getSubject().getPrincipal();
//        String userId = token.getId();
//        SimpleAuthorizationInfo info =  new SimpleAuthorizationInfo();
//        //根据用户ID查询角色（role），放入到Authorization里。
//        /*Map<String, Object> map = new HashMap<String, Object>();
//        map.put("user_id", userId);
//        List<SysRole> roleList = sysRoleService.selectByMap(map);
//        Set<String> roleSet = new HashSet<String>();
//        for(SysRole role : roleList){
//            roleSet.add(role.getType());
//        }*/
//        //实际开发，当前登录用户的角色和权限信息是从数据库来获取的，我这里写死是为了方便测试
//        Set<String> roleSet = new HashSet<>();
//        roleSet.add("100002");
//        info.setRoles(roleSet);
//        //根据用户ID查询权限（permission），放入到Authorization里。
//        /*List<SysPermission> permissionList = sysPermissionService.selectByMap(map);
//        Set<String> permissionSet = new HashSet<String>();
//        for(SysPermission Permission : permissionList){
//            permissionSet.add(Permission.getName());
//        }*/
//        Set<String> permissionSet = new HashSet<String>();
//        permissionSet.add("权限添加");
//        info.setStringPermissions(permissionSet);
//        return info;
        return null;
    }
}
