package com.medolia.secondkill.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessageInfo {
    private Integer id ;
    private Long messageId ;
    private Long userId ;
    private String content ;
    private Date createTime;
    private Integer status ;
    private Date overTime ;
    private Integer messageType ;
    private Integer sendType ;
    private String goodName ;
    private BigDecimal price ;
    private String messageHead ;
}
