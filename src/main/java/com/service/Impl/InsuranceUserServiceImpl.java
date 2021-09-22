package com.service.Impl;

import com.mapper.InsuranceUserMapper;
import com.po.InsuranceUser;
import com.service.InsuranceUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InsuranceUserServiceImpl implements InsuranceUserService {
    @Autowired
    private InsuranceUserMapper insuranceUserMapper;
    @Override
    public int insert(InsuranceUser user) {
        return insuranceUserMapper.insert(user);
    }

    @Override
    public InsuranceUser findByCode(String UserCode) {
        return insuranceUserMapper.findByCode(UserCode);
    }

    @Override
    public int upact(String UserCode) {
        return insuranceUserMapper.upact(UserCode);
    }
}
