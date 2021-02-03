package com.medolia.secondkill.access;

import com.medolia.secondkill.domain.SeckillUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContext {

    private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<>();

    public static void setUser(SeckillUser user) {
        // log.info("user stored in threadLocal variable: " + user);
        userHolder.set(user);
    }

    public static SeckillUser getUser() {
        return userHolder.get();
    }
}
