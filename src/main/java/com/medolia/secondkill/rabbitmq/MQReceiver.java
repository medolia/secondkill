package com.medolia.secondkill.rabbitmq;

import com.medolia.secondkill.domain.SeckillOrder;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.OrderService;
import com.medolia.secondkill.service.SeckillService;
import com.medolia.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    RedisService redisService;
    GoodsService goodsService;
    OrderService orderService;
    SeckillService seckillService;

    @Autowired
    public MQReceiver(RedisService redisService, GoodsService goodsService,
                      OrderService orderService, SeckillService seckillService) {
        this.redisService = redisService;
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.seckillService = seckillService;
    }

    /**
     * 消费队列逻辑解释（对应未集成 rabbitMQ 前的秒杀逻辑）
     * 1. 数据库判断是否库存为空 ？ yes ：提前返回（消息处理结束） no ：跳转至 2
     * 2. 数据库判断是否重复下单 ？ yes ：提前返回 no ：跳转至 3
     * 3. 事务操作：减库存，下订单，写入秒杀订单
     */
    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receive(String message) {
        log.info("msg received in seckill queue: " + message);
        SeckillMsg sm = RedisService.stringToBean(message, SeckillMsg.class);
        SeckillUser user = sm.getUser();
        long goodsId = sm.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        // 判断库存
        if (stock <= 0) return;

        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        // 判断重复下单
        if (order != null) return;

        // 减库存 下订单 写入秒杀订单
        seckillService.seckill(user, goods);
    }

    @RabbitListener(queues = MQConfig.TEST_QUEUE)
    public void receiveTestMsg(String msg) {
        log.info("msg received in test queue: " + msg);
    }
}
