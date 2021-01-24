package com.medolia.secondkill.service;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillService {
    GoodsService goodsService;
    OrderService orderService;

    @Autowired
    public SeckillService(GoodsService goodsService, OrderService orderService) {
        this.goodsService = goodsService;
        this.orderService = orderService;
    }

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        goodsService.reduceStock(goods); // 减库存
        return orderService.createOrder(user, goods); // 下订单
    }
}
