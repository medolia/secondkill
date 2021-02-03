package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.domain.User;
import com.medolia.secondkill.rabbitmq.MQSender;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.UserKey;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.SeckillUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/demo")
public class TestController {

    SeckillUserService userService;
    RedisService redisService;
    MQSender sender;

    @Autowired
    public TestController(SeckillUserService userService, RedisService redisService, MQSender sender) {
        this.userService = userService;
        this.redisService = redisService;
        this.sender = sender;
    }

    @RequestMapping("/model")
    @ResponseBody
    public Result<Boolean> model(Model model) {
        model.addAttribute("greeting", "hello");
        return Result.success(true);
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        log.info("msg sent: test msg.");
        sender.sendTestMsg("test msg");
        return Result.success("msg sent!");
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<String> getUser() {
        SeckillUser user = userService.getById(1);
        return Result.success(user.getNickname());
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
