package com.medolia.secondkill.redis.key;

public interface KeyPrefix {
    int expireSeconds();

    String getPrefix();
}
