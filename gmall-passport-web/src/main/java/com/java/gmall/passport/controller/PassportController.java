package com.java.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.UserInfo;
import com.java.gmall.passport.config.JwtUtil;
import com.java.service.UserService;
import com.java.gmall.util.LoginRequire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {

    @Value("${token.key}")
    String key;

    @Reference
    UserService userService;

    @RequestMapping(value = {"/index","/"})
    public String toIndex(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
//    @LoginRequire
    public String doLogin(HttpServletRequest request, UserInfo userInfo){
        String remoteAddr = request.getHeader("X-forwarded-for");
        System.out.println(userInfo);
        if (userInfo != null) {
            UserInfo user = userService.login(userInfo);
            if(user != null){
                Map<String, Object> map = new HashMap<>();
                map.put("userId",user.getId());
                map.put("nickName",user.getNickName());
                String token = JwtUtil.encode(key, map, remoteAddr);
                return token;
            }
        }
        return "fail";
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");

        if(token != null && token.length() > 0){
            Map<String, Object> map = JwtUtil.decode(token, key, currentIp);
            String userId = (String) map.get("userId");
            UserInfo user = userService.verify(userId);
            if(user != null){
                return "success";
            }
        }
        return "fail";
    }
}
