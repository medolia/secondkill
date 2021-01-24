package com.medolia.secondkill.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginVo {
    private String mobile;
    private String password;

    @Override
    public String toString() {
        return "LoginVo {mobile=" + mobile + ", password=" + password + "}";
    }
}
