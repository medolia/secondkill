package com.medolia.secondkill.config;

import com.medolia.secondkill.access.UserContext;
import com.medolia.secondkill.domain.SeckillUser;
import com.medolia.secondkill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义一个变量解析器
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    SeckillUserService userService;

    @Autowired
    public void setUserService(SeckillUserService userService) {
        this.userService = userService;
    }

    @Override // 当传入参存在类型为 SeckillUser 的变量时触发自定义解析
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz==SeckillUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
          NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // 返回线程局部变量中的 user
        return UserContext.getUser();
    }

}
