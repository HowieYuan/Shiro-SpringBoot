package com.howie.shiro.mapper;

import org.springframework.stereotype.Repository;

/**
 * Created with IntelliJ IDEA
 *
 * @Author yuanhaoyue swithaoy@gmail.com
 * @Description
 * @Date 2018-03-25
 * @Time 22:04
 */
@Repository
public interface UserMapper {
    String getPassword(String username);
}
