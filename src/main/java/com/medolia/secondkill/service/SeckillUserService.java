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

/**
 * 为使对象缓存生效， 一般 service 不能调用其他 dao
 *
 * cache pattern 为经典的 cache aside pattern:
 * 失效：应用程序先从cache取数据，没有得到，则从数据库中取数据，成功后，放到缓存中。
 * 命中：应用程序从cache中取数据，取到后返回。
 * 更新：先把数据存到数据库中，成功后，再让缓存失效。
 */
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
        // 查询对象缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
        if (user != null) {
            log.info("user found in cache: " + user.toString());
            return user;
        }

        // 若缓存未命中，查询数据库、更新缓存
        user = seckillUserDao.getById(id);
        if (user != null)
            redisService.set(SeckillUserKey.getById, ""+id, user);

        return user;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) return null;

        SeckillUser user = redisService.get(SeckillUserKey.token,
                token, SeckillUser.class);
        // 缓存更新
        if (user != null) addCookie(response, token, user);

        return user;
    }

    public boolean updatePassword(String token, long id, String formPass) {
        // 取得用户对象
        SeckillUser user = getById(id);
        if (user == null) throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        // 更新数据库
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        seckillUserDao.update(toBeUpdate);

        // 使缓存失效
        redisService.delete(SeckillUserKey.getById, ""+id); // 使 id 对象缓存失效
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SeckillUserKey.token, token, user); // 更新 token 对象缓存
        return true;
    }

    public String login(HttpServletResponse response, @Valid LoginVo loginVo) {
        if (loginVo == null)
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        // 判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        // 核对密码
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        if (!calcPass.equals(dbPass))
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        // 生成 cookie，即使用一个统一的标识键、一个 uuid 为值存入 cookie 中
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setPath("/");
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        response.addCookie(cookie);
    }
}
