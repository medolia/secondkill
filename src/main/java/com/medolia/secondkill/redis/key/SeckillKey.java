package com.medolia.secondkill.redis.key;

public class SeckillKey extends BaseKeyPrefix {

    private static final int VERIFY_CODE_EXPIRE_SECOND = 300;

    private SeckillKey(String prefix) {
        super(prefix);
    }

    private SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKey seckillOver = new SeckillKey("so");
    public static SeckillKey getSeckillPath = new SeckillKey("sp");
    public static SeckillKey getSeckillVerifyCode = new SeckillKey(
            VERIFY_CODE_EXPIRE_SECOND,"vc");
}
