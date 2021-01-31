package com.medolia.secondkill.vo;

import com.medolia.secondkill.domain.SeckillUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsDetailVo {
    private int seckillStatus;
    private int remainSeconds;
    private GoodsVo goods;
    private SeckillUser user;
}
