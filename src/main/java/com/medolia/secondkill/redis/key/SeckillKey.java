package com.medolia.secondkill.redis.key;

public class SeckillKey extends BaseKeyPrefix {
    private SeckillKey(String prefix) {
        super(prefix);
    }

    public static SeckillKey seckillOver = new SeckillKey("so");
}
