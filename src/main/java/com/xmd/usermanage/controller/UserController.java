package com.xmd.usermanage.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xmd.usermanage.common.BaseResponse;
import com.xmd.usermanage.common.ErrorCode;
import com.xmd.usermanage.common.ResultUtils;
import com.xmd.usermanage.exception.BusinessException;
import com.xmd.usermanage.model.User;
import com.xmd.usermanage.model.request.UserLoginRequest;
import com.xmd.usermanage.model.request.UserRegisterRequest;
import com.xmd.usermanage.model.request.UserSearchRequest;
import com.xmd.usermanage.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.xmd.usermanage.constant.UserConstant.ADMIN_ROLE;
import static com.xmd.usermanage.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        String userName = userRegisterRequest.getUserName();
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode)){
           // return null;
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long l = userService.userRegister(userAccount, userPassword, checkPassword, planetCode,userName);
        return ResultUtils.success(l);

    }

    @PostMapping("/login")
    public BaseResponse<User> doLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.doLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(user);
    }


        @PostMapping("/logout")
        public BaseResponse<Integer> doLogout(HttpServletRequest request){
            if(request == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);

            }
            Integer i = userService.doLogout(request);
            return ResultUtils.success(i);
        }

        @PostMapping("/search")
     public BaseResponse<List<User>> userSearch(@RequestBody UserSearchRequest userSearchRequest, HttpServletRequest request){
        //1，判断是否为管理员

            if(!isAdmin( request)){
                throw new BusinessException(ErrorCode.NO_AUTH); //返回一个空列表
            }
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if(userSearchRequest!= null && StringUtils.isNotBlank(userSearchRequest.getUserName())){
             queryWrapper.like("username", userSearchRequest.getUserName());
            }

            List<User> list = userService.list(queryWrapper);
            return ResultUtils.success(list);
     }


     @PostMapping("/delete")
     public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest  request){
         //1，判断是否为管理员

         if(!isAdmin(request)){
             throw new BusinessException(ErrorCode.NO_AUTH);
         }
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
         boolean b = userService.removeById(id);
         return ResultUtils.success(b);
     }


     //判断是否为管理员
     public boolean isAdmin(HttpServletRequest request){
         Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
         log.info("Session 中的用户对象: {}", userObj);
         if(userObj == null){
             log.warn("用户未登录，Session 为空");
             return false;
         }
         User obj = (User) userObj;
         log.info("用户角色 role: {}", obj.getRole());
         return obj != null && obj.getRole() == ADMIN_ROLE;
     }
}
