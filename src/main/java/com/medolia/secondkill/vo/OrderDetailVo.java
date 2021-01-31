package com.medolia.secondkill.vo;

import com.medolia.secondkill.domain.OrderInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;
}
