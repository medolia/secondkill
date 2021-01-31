package com.medolia.secondkill.redis.key;

public abstract class BaseKeyPrefix implements KeyPrefix {
    private int expireSeconds;
    private String prefix;

    public BaseKeyPrefix(String prefix) {
        this(0, prefix);
    }

    public BaseKeyPrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    public int expireSeconds() { // 0 代表永不过期
        return expireSeconds;
    }

    public String getPrefix() {
        String className = this.getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
