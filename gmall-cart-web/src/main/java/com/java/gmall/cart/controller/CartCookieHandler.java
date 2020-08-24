package com.java.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.java.gmall.bean.CartInfo;
import com.java.gmall.bean.SkuInfo;
import com.java.service.ManageSerivce;
import com.java.gmall.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    private ManageSerivce manageService;

    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;
    }


    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        //定义一个集合存储将来要放到cookie中的数据
        List<CartInfo> cartInfoList = new ArrayList<>();

        boolean isExists = false;
        if (cookieValue != null) {
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    //找到有相同的,增加数量，并设置实时价格
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    isExists = true;
                }
            }
        }

        if (!isExists) {
            SkuInfo skuInfoByDB = manageService.getSkuInfo(skuId);

            //若标识符没有变动，就说明cookie中没有对应的商品,应该获取数据库数据并保存创建的cartInfo对象存入到cartInfoList集合中
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfoByDB.getPrice());
            cartInfo.setSkuPrice(skuInfoByDB.getPrice());
            cartInfo.setSkuName(skuInfoByDB.getSkuName());
            cartInfo.setImgUrl(skuInfoByDB.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);

        }

        //将集合中的数据写入到cookie中去
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);

    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, cookieCartName);

    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        // 循环比较
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)) {
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);
    }

}
