package com.java.service;

import com.java.gmall.bean.CartInfo;

import java.util.List;

public interface CartInfoService {
    void addToCart(String skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId);

    List<CartInfo> mergeToCartList(List<CartInfo> cookieCartInfoList, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
