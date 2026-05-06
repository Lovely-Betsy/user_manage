package com.xmd.usermanage.service.impl;
import java.util.Date;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmd.usermanage.common.ErrorCode;
import com.xmd.usermanage.exception.BusinessException;
import com.xmd.usermanage.model.User;
import com.xmd.usermanage.service.UserService;
import com.xmd.usermanage.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xmd.usermanage.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 21317
* @description 针对表【user】的数据库操作Service实现
* @createDate 2026-04-16 23:28:51
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "xmd";

    @Override
    public  Long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode,String userName) {
        //1.校验
        /*if(userAccount == null || userPassword==null || checkPassword==null){
            return -1;
        }*/
        //上面这种写法看起来有些冗余，下面这种写法更简洁，调用了
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode,userName)){
            //return -1L;
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"还有选项为空，所有选项都要填");
        }

        if(userAccount.length()<4){
           // return -1L;
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度不能小于4");
        }
        if(userPassword.length()<6 || checkPassword.length()<6){
            //return -1L;
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于6");
        }
        if(planetCode.length()>5){
           // return -1L;
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号长度不能大于5");
        }


        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(validPattern);
        Matcher matcher = pattern.matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }


        //账号不能重复
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("userAccount",userAccount);
        long count = userMapper.selectCount(query);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }

        //星球编号不能重复
        query = new QueryWrapper<>();
        query.eq("planetCode",planetCode);
        long count1 = userMapper.selectCount(query);
        if(count1>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号已存在");
        }

       /* //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(validPattern);
        Matcher matcher = pattern.matcher(userAccount);
        if(matcher.find()){
            return -1;
        }*/

        //检验密码和校验密码是否相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码不一致");
        }

        //对密码加密

        String newPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        user.setUserName(userName);
       /* boolean result = this.save(user);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存失败");
        }*/

        //对上面这种写法的改进
        try {
            boolean result = this.save(user); // 调用 IRepository 默认 save
            if (!result) {
                // 极少数情况：insert 返回 0（如主键已存在但无唯一索引）
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "保存失败：数据可能重复或无效");
            }
        } catch (DuplicateKeyException e) {
            // 明确捕获唯一键冲突（最常见业务失败）
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        } catch (DataIntegrityViolationException e) {
            // 其他数据完整性问题（如必填字段为空）
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据校验失败：" + e.getMessage());
        } catch (Exception e) {
            // 兜底：真正的系统错误
            log.error("保存用户异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统内部错误，请稍后重试");
        }

        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest  request) {
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号，密码不能全为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度不能小于4");
        }
        if(userPassword.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于6");
        }

        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(validPattern);
        Matcher matcher = pattern.matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }


        //对密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());


        //查询用户是否存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("userAccount",userAccount);
        query.eq("userPassword",newPassword);
        User user = userMapper.selectOne(query);
        if(user == null){
            log.info("user failed login");
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户登录失败");
        }

        //用户脱敏
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserName(user.getUserName());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setUserPassword(user.getUserPassword());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(new Date());
        safetyUser.setUpdateTime(new Date());
        safetyUser.setIsDelete(0);
        // ... existing code ...
        safetyUser.setRole(user.getRole());

        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        log.info("登录成功，Session ID: {}, 用户角色: {}", request.getSession().getId(), safetyUser.getRole());

        //要返回脱敏后的用户
        return safetyUser;
    }


    @Override
    public int doLogout(HttpServletRequest request){
        //移除登录状态，就相当于注销用户
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

}





