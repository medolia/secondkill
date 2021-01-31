package com.medolia.secondkill.controller;

import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.GoodsKey;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.service.GoodsService;
import com.medolia.secondkill.service.SeckillUserService;
import com.medolia.secondkill.vo.GoodsDetailVo;
import com.medolia.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {
    SeckillUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    ThymeleafViewResolver thymeleafViewResolver;
    ApplicationContext applicationContext;

    @Autowired
    public GoodsController(SeckillUserService userService, RedisService redisService,
                           GoodsService goodsService, ThymeleafViewResolver thymeleafViewResolver,
                           ApplicationContext applicationContext) {
        this.userService = userService;
        this.redisService = redisService;
        this.goodsService = goodsService;
        this.thymeleafViewResolver = thymeleafViewResolver;
        this.applicationContext = applicationContext;
    }

    /**
     * 缓存 商品列表页面
     */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model, SeckillUser user) { // user 会被自定义解析器解析为缓存中的 user
        model.addAttribute("user", user);

        // 取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) return html;

        // 手动渲染
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) // 加入缓存
            redisService.set(GoodsKey.getGoodsList, "", html);
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(SeckillUser user,
                                        @PathVariable("goodsId") long goodsId) {

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;
        if (now < startAt) {
            seckillStatus = 0; // 秒杀未开始
            remainSeconds = (int) (startAt - now) / 1000;
        } else if (now > endAt) {
            seckillStatus = 2; // 秒杀已结束
            remainSeconds = -1;
        } else {
            seckillStatus = 1; // 秒杀进行中
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setSeckillStatus(seckillStatus);

        return Result.success(vo);
    }
}
