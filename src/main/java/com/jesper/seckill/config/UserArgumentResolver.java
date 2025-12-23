package com.jesper.seckill.config;

import com.jesper.seckill.bean.User;
import com.jesper.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jiangyunxiong on 2018/5/22.
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    UserService userService;

    /**
     * 当参数类型为User才做处理
     *
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //获取参数类型
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == User.class;
    }

    /**
     * 思路：先获取到已有参数HttpServletRequest，从中获取到token，再用token作为key从redis拿到User，而HttpServletResponse作用是为了延迟有效期
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        String headerToken = getTokenFromHeader(request);
        if (StringUtils.hasText(headerToken)) {
            return userService.getByToken(response, headerToken);
        }

        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKIE_NAME_TOKEN);
        if (!StringUtils.hasText(cookieToken) && !StringUtils.hasText(paramToken)) {
            return null;
        }
        String token = !StringUtils.hasText(paramToken) ? cookieToken : paramToken;
        return userService.getByToken(response, token);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization)) {
            String prefix = "Bearer ";
            if (authorization.startsWith(prefix)) {
                String token = authorization.substring(prefix.length()).trim();
                return StringUtils.hasText(token) ? token : null;
            }
        }
        String tokenHeader = request.getHeader(UserService.COOKIE_NAME_TOKEN);
        return StringUtils.hasText(tokenHeader) ? tokenHeader.trim() : null;
    }

    //遍历所有cookie，找到需要的那个cookie
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
