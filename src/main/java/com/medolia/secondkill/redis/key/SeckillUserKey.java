package com.medolia.secondkill.redis.key;

public class SeckillUserKey extends BaseKeyPrefix {

    private static final int TOKEN_EXPIRE = 3600 * 24; // 过期时间为 1 天

    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPIRE, "tk");
    public static SeckillUserKey getById = new SeckillUserKey(0, "id");
}
