package com.medolia.secondkill.controller;

import com.medolia.secondkill.exception.GlobalException;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.SeckillUserService;
import com.medolia.secondkill.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    SeckillUserService userService;
    RedisService redisService;

    @Autowired
    public LoginController(SeckillUserService userService, RedisService redisService) {
        this.userService = userService;
        this.redisService = redisService;
    }

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) throws GlobalException {
        String token = userService.login(response, loginVo);
        return Result.success(token);
    }
}
