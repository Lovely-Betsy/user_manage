package com.xmd.usermanage.service;
import java.util.Date;

import com.xmd.usermanage.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest

class UserServiceTest {

    @Resource
    private UserService userService;



    @Test
    void testAddUser(){
        //创建一个User对象
        User user = new User();
        user.setUserName("");
        user.setEmail("456");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123");
        user.setAvatarUrl("https://www.tuhenmei.com/touxiang/125622.html");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        user.setUserStatus(0);

        //User的属性要用setter/getter方法赋值，属性一多，赋值很麻烦，此时就用到了GenerateAllSetter这个插件

        boolean result = userService.save(user);
        System.out.println(user.getId());
        //添加一个断言
        assertTrue(result);

    }


    @Test
    void testDigest(){
        String newPassword = DigestUtils.md5DigestAsHex("123".getBytes());
        System.out.println(newPassword);
    }
}