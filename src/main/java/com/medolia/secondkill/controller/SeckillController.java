package com.medolia.secondkill.controller;

import com.medolia.secondkill.access.AccessLimit;
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
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
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
     * 1. 正确回答验证码问题
     * 2. 生成 path uuid
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    public Result<String> getSeckillPath(SeckillUser user, @RequestParam("goodsId") long goodsId,
                                         @RequestParam("verifyCode") int verifyCode) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);

        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check)
            return Result.error(CodeMsg.SECKILL_VERIFY_FAIL);

        String path = seckillService.createSeckillPath(user, goodsId);
        return Result.success(path);
    }

    /**
     * 秒杀逻辑（入队前）解释
     * 1. user 已登录，否则返回 会话期过时
     * 2. path 值核对为真，否则返回 请求非法
     * 2. 内存标记（map 对象）表明商品仍在秒杀，否则返回 秒杀结束
     * 3. 访问缓存库存（redis 更新值）是否还有余量 否则更新对应商品的内存标记，返回 秒杀结束
     * 4. 数据库判断未重复下单 否则返回 重复秒杀
     * 5. 将消息发送（direct exchange）至 id 为 "SECKILL_QUEUE" 的消息队列 queue，返回 秒杀等待中
     */
    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<CodeMsg> seckill(SeckillUser user, @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path") String path) {

        // 用户登录验证
        if (user == null) return Result.error(CodeMsg.SESSION_ERROR);

        // path 验证
        log.info("path: " + path);
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);

        // 内存标记，减少 redis 访问
        boolean seckillOver = localOverMap.get(goodsId);
        log.info("whether seckill is over confirmed by local memory: " + seckillOver);
        if (seckillOver) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 访问缓存中的库存
        Long stock = redisService.get(GoodsKey.getSeckillGoodsStock, "" + goodsId, Long.class);
        log.info("stock obtained from redis: " + stock);
        if (stock <= 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 判断重复秒杀
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        log.info(order == null ? "new seckill order" : "seckill repeated");
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

    /**
     * 生成图形验证码
     */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, SeckillUser user,
                                               @RequestParam("goodsId") long goodsId) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);

        try {
            BufferedImage image = seckillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }
}
