package com.controller;

import com.alibaba.fastjson.JSON;
import com.po.Dto;
import com.po.InsuranceUser;
import com.service.Impl.InsuranceUserServiceImpl;
import com.util.*;
import com.util.vo.InsuranceUserAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/login")
public class InsuranceUserLoginController {
    @Autowired
    private InsuranceUserServiceImpl insuranceUserService;
    private Jedis jedis=new Jedis("127.0.0.1",6379);
    /************************用户登陆的方法*******************/
    @RequestMapping(value = "/loginUserCode")
    public Dto loginUserCode(HttpServletResponse response, HttpServletRequest request, @RequestBody InsuranceUserAddVo user) {
        System.out.println("用户登陆的方法。。。" + user.toString());
        try {
            if(user!=null&&user.getUserCode()!=null){
                InsuranceUser olduser=insuranceUserService.findByCode(user.getUserCode());
                if(olduser!=null){//有这个用户
                    //判断用户是否登陆过
                    if(jedis.get(olduser.getUserCode())!=null){//登陆过
                        String oldtoken=jedis.get(olduser.getUserCode());
                        try {
                            //判断相关参数，完成浏览器重复登陆处理
                            String toke=TokenUtil.replaceToken(request.getHeader("user-agent"),oldtoken,response);
                            if(toke!=null){
                                System.out.println("1212121212");
                                return DtoUtil.returnSuccess("登陆成功");
                            }
                        } catch (TokenValidationFailedException e) {
                            e.printStackTrace();
                            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
                        }
                    }else {
                        //1.处理密码
                        String md5str = MD5Util.getMd5(user.getUserPassword(), 32);
                        if (md5str.equals(olduser.getUserPassword()) && olduser.getActivated() == 1) {
                            /*******************实现单点登陆**************************/
                            //获取浏览器头信息--（信息工具类的依赖）
                            String requesthade = request.getHeader("user-agent");
                            System.out.println("浏览器请求头requesthade----" + requesthade);
                            //生成Token
                            String token = TokenUtil.getTokenGenerator(requesthade, olduser);
                            //token存入redis和前端浏览器(cookie)
                            jedis.setex(olduser.getUserCode(), 7200, token);
                            //将token作为Key，对象作为Value。目的是后边判断该用户是否登录
                            jedis.setex(token, 7200, JSON.toJSONString(olduser));
                            Cookie ustoken = new Cookie("token", token);
                            ustoken.setMaxAge(60 * 60 * 2);//设置存储时长两小时
                            response.addCookie(ustoken);//将ustoken存储到cookie
                            return DtoUtil.returnSuccess("登陆成功");
                        } else {
                            System.out.println("密码不正确或未激活");
                            return DtoUtil.returnFail("密码不正确或未激活", ErrorCode.AUTH_AUTHENTICATION_UPDATE);
                        }
                    }
                }else{//没这个用户
                    return DtoUtil.returnFail("用户不存在" , ErrorCode.AUTH_ILLEGAL_USERCODE);
                }
            }else {
                return DtoUtil.returnFail("前台传递数据错误:" , ErrorCode.AUTH_AUTHENTICATION_UPDATE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //e.getMessage()是异常信息
            return DtoUtil.returnFail("用户登陆异常:" + e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
        return DtoUtil.returnSuccess("登陆成功");
    }
}
