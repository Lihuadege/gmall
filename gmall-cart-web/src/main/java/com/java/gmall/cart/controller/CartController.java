package com.java.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.java.gmall.bean.CartInfo;
import com.java.gmall.bean.SkuInfo;
import com.java.service.CartInfoService;
import com.java.service.ManageSerivce;
import com.java.gmall.util.CookieUtil;
import com.java.gmall.util.LoginRequire;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {
    // 定义购物车名称
    private String cookieCartName = "CART";

    @Reference
    ManageSerivce manageSerivce;

    @Reference
    CartInfoService cartInfoService;

    @Autowired
    CartCookieHandler cartCookieHandler;

    /**
     * 点击去结算，跳转到结算页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList = cartCookieHandler.getCartList(request);
        if (cookieHandlerCartList != null && cookieHandlerCartList.size() > 0) {
            cartInfoService.mergeToCartList(cookieHandlerCartList, userId);
            cartCookieHandler.deleteCartCookie(request, response);
        }
        return "redirect://order.gmall.com/trade";
    }

        /**
         * 勾选或者取消勾选商品的控制器
         * @param request
         * @param response
         * @return
         */
    @RequestMapping("/checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request, HttpServletResponse response){

        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");

        if (userId!=null){
            cartInfoService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    /**
     * 查询购物车列表，分为登录和未登录2种
     * @return
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, Model model){

        String userId = (String)request.getAttribute("userId");

        //定义一个集合存储将要展示的所有在购物车中的商品
        List<CartInfo> cartInfoList = new ArrayList<>();

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        if(userId != null){
            //不为空，说明用户登录，这个需要合并购物车
            if(!StringUtils.isEmpty(cookieValue)){
                //若cookie不是空的，合并购物车
                // 开始合并
                List<CartInfo> cookieCartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
                cartInfoList=cartInfoService.mergeToCartList(cookieCartInfoList,userId);
                // 删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                //若是空的，就直接查询redis或数据库即可
                cartInfoList = cartInfoService.getCartList(userId);
            }
            request.setAttribute("cartInfoList",cartInfoList);
        }else{
            //为空，说明还未登录，这个不需要合并购物车,好写一些
            //直接查询cookie中所有的缓存的商品，然后展示即可
            if (!StringUtils.isEmpty(cookieValue)) {
                cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            }
        }

//        request.setAttribute("cartInfo",cartInfoList);
        model.addAttribute("cartList",cartInfoList);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @param request
     * @param response
     * @return
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("/addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, Model model){

        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        String userId = (String)request.getAttribute("userId");

        if(userId != null){
            //若userId不是空，用户登录状态下添加购物车
            cartInfoService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            //为空，将购物车信息放在cookie中
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }

        SkuInfo skuInfo = manageSerivce.getSkuInfo(skuId);

        model.addAttribute("skuInfo",skuInfo);
        model.addAttribute("skuNum",skuNum);

//        request.setAttribute("skuInfo",skuInfo);
//        request.setAttribute("skuNum",skuNum);

        return "success";
    }

}
