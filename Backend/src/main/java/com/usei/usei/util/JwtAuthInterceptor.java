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

        String path = request.getRequestURI();

        // RUTAS PÚBLICAS
        if (path.equals("/auth/login") ||
                path.startsWith("/estudiante/enviarCodigoVerificacion") ||
                path.startsWith("/usuario/enviarCodigoVerificacion")) {
            return true;
        }

        // MÓDULO DE SEGURIDAD SIN TOKEN (tu decisión actual)
        if (path.startsWith("/usuario") || path.startsWith("/rol")) {
            return true;
        }

        // Recursos estáticos
        if (path.startsWith("/documents/") ||
                path.startsWith("/imagenes/") ||
                path.equals("/noticia/carrusel")) {
            return true;
        }

        // VALIDACIÓN DE TOKEN
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token no proporcionado\",\"message\":\"Debe autenticarse para acceder a este recurso\"}");
            return false;
        }

        // ✅ Pasamos el header completo (TokenGenerator recorta Bearer)
        Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token inválido o expirado\",\"message\":\"Su sesión ha expirado, por favor inicie sesión nuevamente\"}");
            return false;
        }

        // Guardar datos para uso posterior
        request.setAttribute("userId", claims.getBody().get("id"));
        request.setAttribute("userType", claims.getBody().get("type"));
        request.setAttribute("username", claims.getBody().get("username"));

        return true;
    }
}
