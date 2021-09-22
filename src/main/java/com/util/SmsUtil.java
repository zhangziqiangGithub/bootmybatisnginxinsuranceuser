package com.util;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.po.InsuranceUser;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class SmsUtil {//发送短信工具类
    public static String checksms(InsuranceUser user) {
        System.out.println("发送短信方法运行。。。。");
        //初始化sdk
        CCPRestSmsSDK ccpRestSmsSDK = new CCPRestSmsSDK();
        //初始化服务器端口和地址
        ccpRestSmsSDK.init("app.cloopen.com", "8883");
        //设置账号和token
        ccpRestSmsSDK.setAccount("8a216da87ba59937017bec8aa0a210a1", "1028ea91681c4a9abcdc7ddfa9924914");
        //设置appid
        ccpRestSmsSDK.setAppId("8a216da87ba59937017bec8aa19910a8");
        //验证码处理
        //获取系统当前时间
        Date date = new Date();
        long datetime = date.getTime();
        String strdate = datetime + "";
        //处理后的验证码
        String smsdk = strdate.substring(strdate.length() - 4, strdate.length());
        //设置内容和时长
        HashMap<String, Object> result = ccpRestSmsSDK.sendTemplateSMS("18791344231", "1", new String[]{smsdk, "3"});
        //判断短信是否发送成功
        if ("000000".equals(result.get("statusCode"))) {
            //正常返回输出data包体信息（map）
            HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for (String key : keySet) {
                Object object = data.get(key);
                System.out.println(key + " = " + object);
            }
            System.out.println("验证码："+smsdk);
            System.out.println("发送成功");
            return smsdk;
        } else {
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") + " 错误信息= " + result.get("statusMsg"));
            System.out.println("发送失败。。。");
            return null;
        }
    }
}
