package com.xmd.usermanage.common;

public class ResultUtils {

    public static <T> BaseResponse<T> success(T  data){
        return new BaseResponse<>(0,data,"success", "");
    }



    public static <T> BaseResponse<T> error(ErrorCode errorCode,String message,String description){
        return new BaseResponse<>(errorCode.getCode(),null,message,description);
    }



    public static <T> BaseResponse<T> error(int code,String message,String description){
        return new BaseResponse<>(code,null,message,description);
    }
}
