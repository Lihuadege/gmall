package com.java.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.java.gmall.bean.OrderDetail;
import com.java.gmall.bean.OrderInfo;
import com.java.gmall.config.RedisUtil;
import com.java.gmall.order.mapper.OrderDetailMapper;
import com.java.gmall.order.mapper.OrderInfoMapper;
import com.java.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;

    // 生成流水号
    @Override
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    // 验证流水号
    @Override
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }
    // 删除流水号
    @Override
    public void  delTradeCode(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }


    @Override
    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="java"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;

    }
}
