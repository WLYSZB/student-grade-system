package com.example.gradesystem.common;

import com.example.gradesystem.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RoleInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            sendError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            sendError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        String requiredRoles = request.getHeader("X-Required-Role");
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }

        List<String> allowedRoles = Arrays.asList(requiredRoles.split(","));
        if (!allowedRoles.contains(user.getRole())) {
            sendError(response, ErrorCode.FORBIDDEN);
            return false;
        }

        return true;
    }

    private void sendError(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(errorCode.getCode(), errorCode.getMessage())
        ));
    }
}
