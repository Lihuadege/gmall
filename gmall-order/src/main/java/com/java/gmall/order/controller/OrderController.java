package com.java.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.CartInfo;
import com.java.gmall.bean.OrderDetail;
import com.java.gmall.bean.OrderInfo;
import com.java.gmall.bean.UserAddress;
import com.java.gmall.bean.enums.OrderStatus;
import com.java.gmall.bean.enums.ProcessStatus;
import com.java.gmall.util.LoginRequire;
import com.java.service.CartInfoService;
import com.java.service.OrderService;
import com.java.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class OrderController {

    @Reference
    CartInfoService cartInfoService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request, Model model) {
        String userId = (String) request.getAttribute("userId");
        // 检查tradeCode
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            model.addAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }


        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        // 保存
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo
        orderService.delTradeCode(userId);
        // 重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

        @RequestMapping("/trade")
    @LoginRequire
        public String toTrade(HttpServletRequest request, Model model) {

            String userId = request.getParameter("userId");

            // 得到选中的购物车列表
            List<CartInfo> cartCheckedList = cartInfoService.getCartCheckedList(userId);
            // 收货人地址
            List<UserAddress> userAddressList = userService.findAddByUserId(userId);
            model.addAttribute("userAddressList", userAddressList);
            // 订单信息集合
            List<OrderDetail> orderDetailList = new ArrayList<>(cartCheckedList.size());
            for (CartInfo cartInfo : cartCheckedList) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetailList.add(orderDetail);
            }
            model.addAttribute("orderDetailList", orderDetailList);
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderDetailList(orderDetailList);
            orderInfo.sumTotalAmount();
            model.addAttribute("totalAmount", orderInfo.getTotalAmount());

            // 获取TradeCode号
            String tradeNo = orderService.getTradeNo(userId);
            request.setAttribute("tradeCode", tradeNo);


            return "trade";
        }

}
