package com.java.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.java.gmall.bean.UserAddress;
import com.java.gmall.bean.UserInfo;
import com.java.gmall.config.RedisUtil;
import com.java.gmall.user.Mapper.UserAddressMapper;
import com.java.gmall.user.Mapper.UserInfoMapper;
import com.java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Override
    public UserInfo verify(String id) {
        String key = userKey_prefix+id+userinfoKey_suffix;
        Jedis jedis = redisUtil.getJedis();
        try{
            String user = jedis.get(key);
            if(user != null){
                jedis.expire(key,userKey_timeOut);
                UserInfo userInfo = JSON.parseObject(user, UserInfo.class);
                return  userInfo;
            }
        }finally {
            if(jedis!=null)jedis.close();
        }
        return null;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String passwd = userInfo.getPasswd();
        String realPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(realPasswd);
        UserInfo returnUserInfo = userInfoMapper.selectOne(userInfo);
        if(returnUserInfo != null){
            Jedis jedis = redisUtil.getJedis();
            try {
                //将user的信息存入Redis中，以user:userId:info为key
                jedis.setex(userKey_prefix+returnUserInfo.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(returnUserInfo));
            } finally {
                if(jedis != null)jedis.close();
            }
            return returnUserInfo;
        }
        return null;
    }

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
