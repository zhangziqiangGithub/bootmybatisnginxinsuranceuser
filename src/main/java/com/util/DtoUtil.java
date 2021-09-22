package com.util;

import com.po.Dto;

public class DtoUtil {//Dto的工具类，向前台返回不同的数据--参数
    public static String success="true";
    public static String fail="false";
    public static String errorcode="0";
    /***
     * 统一返回成功的DTO  不带提示信息 不带数据
     */
    public static Dto returnSuccess(){
        Dto dto=new Dto();
        dto.setSuccess(success);
        return  dto;
    }
    /***
     * 统一返回成功的DTO 带提示信息 带数据
     */
    public static Dto returnSuccess(String message,Object data){
        Dto dto=new Dto();
        dto.setSuccess(success);
        dto.setMsg(message);
        dto.setErrorCode(errorcode);
        dto.setData(data);
        return  dto;
    }
    /***
     * 统一返回成功的DTO 带提示信息 不带数据
     */
    public static Dto returnSuccess(String message){
        Dto dto=new Dto();
        dto.setSuccess(success);
        dto.setMsg(message);
        dto.setErrorCode(errorcode);
        return  dto;
    }
    /***
     * 统一返回成功的DTO 带数据 没有消息
     */
    public static Dto returnDataSuccess(Object data){
        Dto dto=new Dto();
        dto.setSuccess(success);
        dto.setErrorCode(errorcode);
        dto.setData(data);
        return  dto;
    }
    /**
     * 统一返回失败的DTO 带提示信息和失败或错误编号
     * */
    public static Dto returnFail(String message,String errorCode){
        Dto dto=new Dto();
        dto.setSuccess(fail);
        dto.setMsg(message);
        dto.setErrorCode(errorCode);
        return  dto;
    }
}
