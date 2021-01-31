package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.SeckillOrder;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.rabbitmq.MQSender;
import com.medolia.secondkill.rabbitmq.SeckillMsg;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.GoodsKey;
import com.medolia.secondkill.redis.key.OrderKey;
import com.medolia.secondkill.redis.key.SeckillKey;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.OrderService;
import com.medolia.secondkill.service.SeckillService;
import com.medolia.secondkill.service.SeckillUserService;
import com.medolia.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    SeckillUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    OrderService orderService;
    SeckillService seckillService;
    MQSender sender;

    @Autowired
    public SeckillController(SeckillUserService userService, RedisService redisService,
                             GoodsService goodsService, OrderService orderService,
                             SeckillService seckillService, MQSender sender) {
        this.userService = userService;
        this.redisService = redisService;
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.seckillService = seckillService;
        this.sender = sender;
    }

    // 使用一个本地变量 记录是否秒杀已经结束 减少对数据库的访问
    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始：把商品库存数量加载到 redis
     * AOP 原理，初始化自动调用方法
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) return;

        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    /**
     * 秒杀逻辑（入队前）解释
     * 1. user 为 null ？ yes：返回会话期过时或需要登录的错误信息 no：跳转至 2
     * 2. 内存标记（map 对象）表明商品是否秒杀结束 ？ yes：返回秒杀结束的错误信息 no：跳转至 3
     * 3. 预减库存（redis 更新值）是否失败 ？ yes：更新对应商品的内存标记，返回秒杀结束的错误信息 no：跳转值 4
     * 4. 数据库判断是否重复下单 ？ yes：返回重复秒杀的错误信息 no：跳转至 5
     * 5. 将消息发送（direct exchange）至 id 为 "SECKILL_QUEUE" 的消息队列，返回秒杀等待中的成功信息
     */
    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<CodeMsg> seckill(SeckillUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) return Result.error(CodeMsg.SESSION_ERROR);

        // 内存标记，减少 redis 访问
        boolean seckillOver = localOverMap.get(goodsId);
        log.info("whether seckill is over confirmed by local memory: " + seckillOver);
        if (seckillOver) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 预减库存
        // TODO: 单用户重复点击造成库存缓存失效
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        log.info("stock obtained from redis: " + stock);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 判断重复秒杀
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        log.info("" + order);
        if (order != null)
            return Result.error(CodeMsg.SECKILL_REPEATED);

        // 入队
        SeckillMsg msg = new SeckillMsg();
        msg.setUser(user);
        msg.setGoodsId(goodsId);
        sender.sendSeckillMessage(msg);

        return Result.success(CodeMsg.SECKILL_WAIT);
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(SeckillUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        long result = seckillService.getSeckillResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getSeckillOrderByUidGid);
        redisService.delete(SeckillKey.seckillOver);
        seckillService.reset(goodsList);
        return Result.success(true);
    }
}
