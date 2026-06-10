package com.example.final_project.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            return true;
        }

        String redirectUrl = URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8);
        response.sendRedirect("/login?redirect=" + redirectUrl);
        return false;
    }
}
