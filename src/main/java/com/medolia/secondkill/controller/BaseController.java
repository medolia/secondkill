package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.User;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.UserKey;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class BaseController {

    UserService userService;
    RedisService redisService;

    @Autowired
    public BaseController(UserService userService, RedisService redisService) {
        this.userService = userService;
        this.redisService = redisService;
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<String> getUser() {
        User user = userService.getById(1);
        return Result.success(user.getName());
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> home() {
        return Result.success("Welcome to medolia seckill system");
    }

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SESSION_ERROR);
    }

    @RequestMapping("redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user = redisService.get(UserKey.getById, "1", User.class);

        return Result.success(user);
    }

    @RequestMapping("redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User(1L, "nelfmis");
        redisService.set(UserKey.getById, "1", user); // "UserKey:id1"
        return Result.success(true);
    }
}
