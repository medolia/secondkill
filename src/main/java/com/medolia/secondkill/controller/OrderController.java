package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.OrderService;
import com.medolia.secondkill.service.SeckillService;
import com.medolia.secondkill.vo.GoodsVo;
import com.medolia.secondkill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {
    OrderService orderService;
    GoodsService goodsService;

    @Autowired
    public OrderController(OrderService orderService, GoodsService goodsService) {
        this.orderService = orderService;
        this.goodsService = goodsService;
    }

    @RequestMapping (value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(SeckillUser user, @RequestParam("orderId") long orderId) {
        if (user == null) return Result.error(CodeMsg.SESSION_ERROR);

        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) return Result.error(CodeMsg.ORDER_NOT_EXIST);

        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
