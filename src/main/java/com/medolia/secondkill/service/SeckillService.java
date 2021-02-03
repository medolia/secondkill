package com.medolia.secondkill.service;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillOrder;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.SeckillKey;
import com.medolia.secondkill.util.MD5Util;
import com.medolia.secondkill.util.UUIDUtil;
import com.medolia.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

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
        redisService.set(SeckillKey.seckillOver, "" + goodsId, true);
    }

    private boolean getSeckillOver(long goodsId) {
        return redisService.exists(SeckillKey.seckillOver, "" + goodsId);
    }

    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    public String createSeckillPath(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) return null;
        String path = MD5Util.md5(UUIDUtil.uuid() + "pathUUid");
        // 键值例 SeckillKey sp 14521541236_21 : as3486a1c3asa31d65das...
        redisService.set(SeckillKey.getSeckillPath, user.getId() + "_" + goodsId, path);
        return path;
    }

    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if (user == null || path == null)
            return false;
        String oldPath = redisService.get(SeckillKey.getSeckillPath,
                user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }

        // 生成背景板和若干起干扰作用的点
        int width = 80;
        int height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        Random rdm = new Random();
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }

        // 生成并在画板上显示问题
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        // 调用 js 引擎计算答案
        int ans = calc(verifyCode);
        // 问题：答案 存入 redis
        redisService.set(SeckillKey.getSeckillVerifyCode, user.getId() + "_" + goodsId, ans);
        return image;
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    /**
     * 使用 加减乘 三个运算
     */
    private static String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        int num2 = rdm.nextInt(10);
        char op2 = ops[rdm.nextInt(3)];
        int num3 = rdm.nextInt(10);
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (int) engine.eval(verifyCode);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 检查验证码
     * 若验证码核对成功，须将 redis 中的对应记录删除防止被重复利用
     */
    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0)
            return false;

        Integer codePre = redisService.get(SeckillKey.getSeckillVerifyCode,
                user.getId() + "_" + goodsId, Integer.class);

        if (codePre == null || codePre - verifyCode != 0)
            return false;

        redisService.delete(SeckillKey.getSeckillVerifyCode,
                user.getId() + "_" + goodsId);
        return true;
    }

}
