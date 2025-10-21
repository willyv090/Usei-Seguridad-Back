package com.usei.usei.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permitir OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Rutas públicas (sin autenticación)
        String path = request.getRequestURI();
        if (path.equals("/auth/login") || path.startsWith("/public/")) {
            return true;
        }

        // Validar token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token no proporcionado\"}");
            return false;
        }

        Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token inválido o expirado\"}");
            return false;
        }

        // Guardar datos del usuario en el request
        request.setAttribute("userId", claims.getBody().get("id"));
        request.setAttribute("userType", claims.getBody().get("type"));
        request.setAttribute("username", claims.getBody().get("username"));

        return true;
    }
}