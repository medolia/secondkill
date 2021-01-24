package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.SeckillUserService;
import com.medolia.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {
    SeckillUserService userService;
    RedisService redisService;
    GoodsService goodsService;

    @Autowired
    public GoodsController(SeckillUserService userService,
                           RedisService redisService, GoodsService goodsService) {
        this.userService = userService;
        this.redisService = redisService;
        this.goodsService = goodsService;
    }

    @RequestMapping("/to_list")
    public String list(Model model, SeckillUser user) { // user 会被自定义解析器解析为缓存中的 user
        model.addAttribute("user", user);
        log.info("new user in model: " + (user == null ? "no one" : user.getNickname()));
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVoList);
        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String toDetail(Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSec = 0;
        if (now < startAt) {
            seckillStatus = 0; // 秒杀未开始
            remainSec = (int) (startAt - now) / 1000;
        } else if (now > endAt) {
            seckillStatus = 2; // 秒杀已结束
            remainSec = -1;
        } else {
            seckillStatus = 1; // 秒杀进行中
            remainSec = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSec", remainSec);

        return "goods_detail";
    }
}
