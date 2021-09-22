package com.service;

import com.po.InsuranceUser;

public interface InsuranceUserService {
    /*用户注册--添加*/
    public int insert(InsuranceUser user);
    /*查询单个用户(注册前判断该用户是否存在)*/
    public InsuranceUser findByCode(String UserCode);
    /*激活的方法  修改activated字段的值*/
    public int upact(String UserCode);
}
