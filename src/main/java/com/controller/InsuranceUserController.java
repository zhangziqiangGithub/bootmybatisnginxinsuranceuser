package com.controller;

import com.po.Dto;
import com.po.InsuranceUser;
import com.service.Impl.InsuranceUserServiceImpl;
import com.util.*;
import com.util.vo.InsuranceUserAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Date;

@RestController
@RequestMapping(value = "/api")
public class InsuranceUserController {
    @Autowired
    private InsuranceUserServiceImpl insuranceUserService;
    private Jedis jedis=new Jedis("127.0.0.1",6379);
    /************************用户注册的方法*******************/
    @RequestMapping(value = "/save_insuranceUser")
    public Dto saveUser(@RequestBody InsuranceUserAddVo user) {
        System.out.println("用户自注册的方法。。。" + user.toString());
        try {
            //判断注册的账号是否已存在
            if (user != null && user.getUserCode() != null) {
                InsuranceUser us = insuranceUserService.findByCode(user.getUserCode());
                if (us != null) {
                    if(us.getActivated()==1){//该用户已注册，已激活
                        return DtoUtil.returnFail("注册失败！！该账号已存在", ErrorCode.AUTH_PARAMETER_ERROR);
                    }else{//用户存在未激活（重新发送激活码）
                        sendck(us);
                        return DtoUtil.returnFail("注册失败！！该账号已存在未激活，已重新发送验证码去激活页面", ErrorCode.AUTH_REPLACEMENT_FAILED);
                    }

                } else {
                    //处理传递过来的数据
                    InsuranceUser olduser = new InsuranceUser();
                    olduser.setUserCode(user.getUserCode());
                    //处理密码
                    String pas = user.getUserPassword();
                    String pass = MD5Util.getMd5(pas, 32);
                    olduser.setUserPassword(pass);
                    olduser.setUserName(user.getUserName());
                    olduser.setWeChat(user.getWeChat());
                    olduser.setIdnumber(user.getIdnumber());
                    //自处理数据
                    olduser.setUserType(1);
                    olduser.setCreationDate(new Date());
                    olduser.setCreatedBy(user.getUserName());
                    olduser.setModifyDate(new Date());
                    olduser.setModifiedBy(user.getUserName());
                    olduser.setActivated(0);
                    int code = insuranceUserService.insert(olduser);
                    if (code > 0) {
                        //该用户第一次注册，现在注册成功(发送激活码)
                          //判断是手机号还是邮箱
                        if(olduser.getUserCode().indexOf("@")!=-1){//是邮箱
                            System.out.println("邮箱注册，发送邮件激活");
                            String emilck=EmilUtil.checkEmil(olduser);
                            //将激活码存入redis(缓存验证码)
                            jedis.setex(olduser.getUserCode(),120,emilck);
                        }else{//是手机号
                            System.out.println("手机号注册，发送短信激活");
                            String smsck=SmsUtil.checksms(olduser);
                            //将激活码存入redis(缓存验证码)
                            jedis.setex(olduser.getUserCode(),120,smsck);
                        }
                        return DtoUtil.returnSuccess("注册成功！！！！！");
                    } else {
                        return DtoUtil.returnFail("注册失败后台数据错误！！！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
                    }
                }
            }else{
                return DtoUtil.returnFail("注册失败后台前台传入数据错误！！！", ErrorCode.AUTH_AUTHENTICATION_UPDATE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //e.getMessage()是异常信息
            return DtoUtil.returnFail("用户注册异常:" + e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }
    /********************注册时判断用户是否已存在查询单个**************************/
    @RequestMapping(value = "/findbyid_user/{code}")
    public String findbycode(@PathVariable String code){
        System.out.println(code);
        InsuranceUser us=insuranceUserService.findByCode(code);
        if(us!=null){
            return "用户名已存在";
        }else{
            return null;
        }
    }

    /************************用户激活的方法*******************/
    @RequestMapping(value = "/activatByCode/{acode}")
    public Dto activatByCode(@PathVariable String acode,@RequestBody InsuranceUserAddVo user) {
        System.out.println("用户激活的方法。。。" + user.toString()+"验证码："+acode);
        //判断要激活的账号是否存在，是否已经激活
        InsuranceUser us=insuranceUserService.findByCode(user.getUserCode());
        if(us!=null){//有这个账号,可以激活
            if(us.getActivated()==0){//未激活，做激活判断
                //判断验证码是否与redis中的一致
                String jedisck=jedis.get(us.getUserCode());
                if(jedisck.equals(acode)){
                    int code=insuranceUserService.upact(user.getUserCode());
                    return DtoUtil.returnSuccess("激活成功！！");
                }else {
                    if(jedisck==null){
                    sendck(us);
                    return DtoUtil.returnFail("激活失败,验证码错误,重新发送验证码！！！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
                    }else{
                        return DtoUtil.returnFail("激活失败,验证码错误,请重新输入验证码！！！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
                    }
                }
            }else{//返回用户已存在已激活
                return DtoUtil.returnFail("激活失败,该账号已激活！！！", ErrorCode.AUTH_PARAMETER_ERROR);
            }
        }else {//该用户不存在
            return DtoUtil.returnFail("激活失败,该账号未注册！！！", ErrorCode.AUTH_ACTIVATE_FAILED);
        }
    }

    /*发送验证码的方法.。。。。。。。。。。。。。。。。。。。。*/
    private void sendck(InsuranceUser user){
        //判断是手机号还是邮箱
        if(user.getUserCode().indexOf("@")!=-1){//是邮箱
            System.out.println("邮箱注册，发送邮件激活");
            String emilck=EmilUtil.checkEmil(user);
            //将激活码存入redis(缓存验证码)
            jedis.setex(user.getUserCode(),120,emilck);
        }else{//是手机号
            System.out.println("手机号注册，发送短信激活");
            String smsck=SmsUtil.checksms(user);
            //将激活码存入redis(缓存验证码)
            jedis.setex(user.getUserCode(),120,smsck);
        }
    }
}
