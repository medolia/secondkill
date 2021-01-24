package com.medolia.secondkill.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessageUser {
    private Long id ;
    private Long userId ;
    private Long messageId ;
    private String goodId ;
    private Date orderId;
}
