package com.java.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.UserAddress;
import com.java.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @RequestMapping("/trade")
    public String index() {
        return "index";
    }

    @RequestMapping("/findOne")
    @ResponseBody
    public List<UserAddress> findAddByUserId(String userId) {
        return userService.findAddByUserId(userId);
    }

}
