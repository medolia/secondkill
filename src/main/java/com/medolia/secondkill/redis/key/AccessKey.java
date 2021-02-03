package com.medolia.secondkill.redis.key;

public class AccessKey extends BaseKeyPrefix{

    private AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }


    public static AccessKey withExpire(int seconds) {
        return new AccessKey(seconds, "ac");
    }
}
