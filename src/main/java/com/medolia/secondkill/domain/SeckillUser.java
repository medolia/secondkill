package com.medolia.secondkill.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillUser {
    private Long id;
    private String nickname;
    private String password;
    private String salt; //  第二次 MD 5 使用的 SALT
    private String head;
    private Date registerDate;
    private Date lastLoginDate;
    private Integer loginCount;
    @Override
    public String toString() {
        return "LoginInfo{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", head='" + head + '\'' +
                ", registerDate=" + registerDate +
                ", lastLoginDate=" + lastLoginDate +
                ", loginCount=" + loginCount +
                '}';
    }
}
