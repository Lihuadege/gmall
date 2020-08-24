package com.java.service;

import com.java.gmall.bean.UserAddress;
import com.java.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> findAll();

    List<UserAddress> findAddByUserId(String userId);

    UserInfo login(UserInfo userInfo);

    UserInfo verify(String userId);
}
