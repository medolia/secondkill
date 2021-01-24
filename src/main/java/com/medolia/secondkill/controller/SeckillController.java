package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillOrder;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.OrderService;
import com.medolia.secondkill.service.SeckillService;
import com.medolia.secondkill.service.SeckillUserService;
import com.medolia.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController {
    SeckillUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    OrderService orderService;
    SeckillService seckillService;

    @Autowired
    public SeckillController(SeckillUserService userService, RedisService redisService, GoodsService goodsService,
                             OrderService orderService, SeckillService seckillService) {
        this.userService = userService;
        this.redisService = redisService;
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.seckillService = seckillService;
    }

    @RequestMapping("/do_seckill")
    public String list(Model model, SeckillUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) return "login";
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        // 判断库存
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.SECKILL_OVER.getMsg());
            return "seckill_fail";
        }

        // 判断是否秒杀成功（防止重复购买）
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goods.getId());
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.SECKILL_REPEATE.getMsg());
            return "seckill_fail";
        }

        // 运行事务：减库存，下订单，写入秒杀订单
        OrderInfo orderInfo = seckillService.seckill(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        log.info(orderInfo.toString());
        log.info(goods.toString());
        return "order_detail";
    }
}
