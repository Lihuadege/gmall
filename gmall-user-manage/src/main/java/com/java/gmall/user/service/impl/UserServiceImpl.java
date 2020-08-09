package com.java.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.java.gmall.bean.UserAddress;
import com.java.gmall.bean.UserInfo;
import com.java.gmall.user.Mapper.UserAddressMapper;
import com.java.gmall.user.Mapper.UserInfoMapper;
import com.java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findAddByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }
}
