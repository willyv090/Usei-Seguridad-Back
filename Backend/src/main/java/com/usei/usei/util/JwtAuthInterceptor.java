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

        final String path = request.getRequestURI();
        final String method = request.getMethod();

        // ====== RUTAS PÚBLICAS (SIN TOKEN) ======

        // Login y recuperación de contraseña
        if (path.equals("/auth/login")
                || path.startsWith("/estudiante/enviarCodigoVerificacion")
                || path.startsWith("/usuario/enviarCodigoVerificacion")) {
            return true;
        }

        // (IMPORTANTE) /usuario y /rol YA NO son públicos en general.
        // Si dejabas /rol público completo, no tendrás userId y no podrás loguear acciones.
        // Si necesitas endpoints públicos específicos de /usuario, ponlos aquí puntualmente (no todo /usuario).

        // Recursos estáticos
        if (path.startsWith("/documents/")
                || path.startsWith("/imagenes/")
                || path.equals("/noticia/carrusel")) {
            return true;
        }

        // Roles: permitir listar/obtener (GET) sin token si quieres mantenerlo público
        if (path.startsWith("/rol") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // Verificar acceso (normalmente se usa sin token desde el frontend)
        if (path.equals("/rol/verificar-acceso")) {
            return true;
        }

        // ====== VALIDACIÓN DE TOKEN PARA TODO LO DEMÁS ======
        String authHeader = request.getHeader("Authorization");

        // Si NO hay token -> 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token no proporcionado\",\"message\":\"Debe autenticarse para acceder a este recurso\"}");
            return false;
        }

        // Validar token
        Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token inválido o expirado\",\"message\":\"Su sesión ha expirado, por favor inicie sesión nuevamente\"}");
            return false;
        }

        // Guardar datos del usuario en el request para uso posterior
        request.setAttribute("userId", claims.getBody().get("id"));
        request.setAttribute("userType", claims.getBody().get("type"));
        request.setAttribute("username", claims.getBody().get("username"));

        return true;
    }
}
