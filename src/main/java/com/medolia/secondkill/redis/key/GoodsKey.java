package com.medolia.secondkill.redis.key;

public class GoodsKey extends BaseKeyPrefix{
    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
    public static GoodsKey getSeckillGoodsStock = new GoodsKey(0, "gs");
}
