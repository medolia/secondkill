package com.medolia.secondkill.vo;

import com.medolia.secondkill.domain.Goods;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GoodsVo extends Goods {
    private Double seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
