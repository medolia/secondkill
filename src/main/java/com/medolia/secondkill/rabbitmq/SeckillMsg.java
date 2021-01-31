package com.medolia.secondkill.rabbitmq;

import com.medolia.secondkill.domain.SeckillUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeckillMsg {
    private SeckillUser user;
    private long goodsId;
}
