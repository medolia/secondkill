package com.medolia.secondkill.redis.key;

public class SeckillUserKey extends BaseKeyPrefix {

    private static final int TOKEN_EXPIRE = 3600 * 24 * 2; // 过期时间为 2 天

    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPIRE, "tk");
}
