package com.java.service;

import com.java.bean.UserAddress;
import com.java.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> findAll();

    List<UserAddress> findAddByUserId(String userId);

}
