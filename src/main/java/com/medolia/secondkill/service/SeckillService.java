package com.medolia.secondkill.service;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillOrder;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.SeckillKey;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeckillService {

    GoodsService goodsService;
    OrderService orderService;
    RedisService redisService;

    @Autowired
    public SeckillService(GoodsService goodsService, OrderService orderService,
                          RedisService redisService) {
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.redisService = redisService;
    }

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        boolean success = goodsService.reduceStock(goods); // 减库存
        if (success)
            return orderService.createOrder(user, goods);
        else {
            setSeckillOver(goods.getId());
            return null;
        }
    }

    public long getSeckillResult(long userId, long goodsId) {
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
        if (order != null)
            return order.getOrderId();
        else {
            boolean seckillOver = getSeckillOver(goodsId);
            return seckillOver ? -1 : 0;
        }
    }

    private void setSeckillOver(long goodsId) {
        redisService.set(SeckillKey.seckillOver, ""+goodsId, true);
    }

    private boolean getSeckillOver(long goodsId) {
        return redisService.exists(SeckillKey.seckillOver, ""+goodsId);
    }

    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }
}
