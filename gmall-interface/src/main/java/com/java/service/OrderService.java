package com.java.service;

import com.java.gmall.bean.OrderInfo;

public interface OrderService {
    String saveOrder(OrderInfo orderInfo);

    boolean checkTradeCode(String userId, String tradeNo);

    void  delTradeCode(String userId);

    String getTradeNo(String userId);
}
