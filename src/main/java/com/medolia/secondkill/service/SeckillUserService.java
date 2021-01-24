package com.medolia.secondkill.service;

import com.medolia.secondkill.dao.SeckillUserDao;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.exception.GlobalException;
import com.medolia.secondkill.redis.RedisService;
import com.medolia.secondkill.redis.key.SeckillUserKey;
import com.medolia.secondkill.result.CodeMsg;
import com.medolia.secondkill.result.Result;
import com.medolia.secondkill.util.MD5Util;
import com.medolia.secondkill.util.UUIDUtil;
import com.medolia.secondkill.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Service
@Slf4j
public class SeckillUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    SeckillUserDao seckillUserDao;
    RedisService redisService;

    @Autowired
    public SeckillUserService(SeckillUserDao seckillUserDao, RedisService redisService) {
        this.seckillUserDao = seckillUserDao;
        this.redisService = redisService;
    }

    public SeckillUser getById(long id) {
        return seckillUserDao.getById(id);
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) return null;

        SeckillUser user = redisService.get(SeckillUserKey.token,
                token, SeckillUser.class);
        // 缓存更新
        if (user != null) addCookie(response, token, user);

        return user;
    }

    public boolean login(HttpServletResponse response, @Valid LoginVo loginVo) {
        if (loginVo == null)
            throw new GlobalException(CodeMsg.SERVER_ERROR);

        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) // 判断手机号是否存在
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        log.info("user found");
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        if (!calcPass.equals(dbPass)) // 核对密码
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        // 生成 cookie，即使用一个统一的标识键、一个 uuid 为值存入 cookie 中
        String token = UUIDUtil.uuid();
        log.info("new token: " + token);
        addCookie(response, token, user);
        return true;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setPath("/");
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        response.addCookie(cookie);
    }
}
