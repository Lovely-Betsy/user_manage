package com.xmd.usermanage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmd.usermanage.model.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author 21317
* @description 针对表【user】的数据库操作Service
* @createDate 2026-04-16 23:28:51
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @return 用户id
     */

    Long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode,String userName);

    /**
     *
     * @param userAccount 账号
     * @param userPassword 密码
     * @return 用户信息
     */
    User doLogin(String userAccount, String userPassword,HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int doLogout(HttpServletRequest request);

}
