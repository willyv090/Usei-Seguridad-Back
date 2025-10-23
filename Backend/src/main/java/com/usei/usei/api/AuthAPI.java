package com.usei.usei.api;

import com.usei.usei.controllers.AuthenticationService;
import com.usei.usei.dto.request.UnifiedLoginRequest;
import com.usei.usei.dto.response.LoginResponseDTO;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.util.TokenGenerator;
import com.usei.usei.services.CaptchaService; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthAPI {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private CaptchaService captchaService; // ✅ NUEVO

    /**
     * Endpoint unificado de login para Usuario y Estudiante
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UnifiedLoginRequest request) {
        try {
            // ✅ 1. Verificar que venga el captcha desde el frontend
            if (request.getCaptchaToken() == null || request.getCaptchaToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new UnsuccessfulResponse(
                        "400 Bad Request",
                        "El token de reCAPTCHA es obligatorio",
                        "/auth/login"
                    )
                );
            }

            // ✅ 2. Validar el captcha con Google
            boolean captchaValido = captchaService.verifyCaptcha(request.getCaptchaToken());
            if (!captchaValido) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new UnsuccessfulResponse(
                        "403 Forbidden",
                        "Verificación de reCAPTCHA fallida. Por favor, inténtelo de nuevo.",
                        "/auth/login"
                    )
                );
            }

            // ✅ 3. Validar datos del usuario
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UnsuccessfulResponse(
                        "400 Bad Request",
                        "El correo electrónico es requerido",
                        "/auth/login"
                    )
                );
            }

            if (request.getContrasena() == null || request.getContrasena().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UnsuccessfulResponse(
                        "400 Bad Request",
                        "La contraseña es requerida",
                        "/auth/login"
                    )
                );
            }

            // ✅ 4. Intentar autenticar
            Map<String, Object> authResult = authService.authenticate(
                request.getCorreo().trim(),
                request.getContrasena()
            );

            Boolean success = (Boolean) authResult.get("success");

            if (!success) {
                String message = (String) authResult.get("message");
                Boolean expired = (Boolean) authResult.getOrDefault("expired", false);
                Boolean bloqueado = (Boolean) authResult.getOrDefault("bloqueado", false);

                HttpStatus status;
                String statusCode;

                if (bloqueado) {
                    status = HttpStatus.LOCKED;
                    statusCode = "423 Locked";
                } else if (expired) {
                    status = HttpStatus.FORBIDDEN;
                    statusCode = "403 Forbidden";
                } else {
                    status = HttpStatus.UNAUTHORIZED;
                    statusCode = "401 Unauthorized";
                }

                UnsuccessfulResponse response = new UnsuccessfulResponse(
                    statusCode,
                    message,
                    "/auth/login"
                );

                return ResponseEntity.status(status).body(response);
            }

            // ✅ 5. Login exitoso
            String tipo = (String) authResult.get("tipo");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) authResult.get("data");
            @SuppressWarnings("unchecked")
            List<String> accesos = (List<String>) authResult.get("accesos");

            String token = "";

            if ("usuario".equals(tipo)) {
                String idUsuario = String.valueOf(data.get("id_usuario"));
                String correoUsuario = (String) data.get("correo");

                token = tokenGenerator.generateToken(
                    idUsuario,
                    "usuario",
                    correoUsuario,
                    60
                );

            } else if ("estudiante".equals(tipo)) {
                String idEstudiante = String.valueOf(data.get("id_estudiante"));
                String correoEstudiante = (String) data.get("correo");

                token = tokenGenerator.generateToken(
                    idEstudiante,
                    "estudiante",
                    correoEstudiante,
                    60
                );
            }

            if (!data.containsKey("accesos") && accesos != null) {
                data.put("accesos", accesos);
            }

            LoginResponseDTO response = new LoginResponseDTO(
                "200 OK",
                "Inicio de sesión correcto",
                token,
                60,
                data,
                accesos
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UnsuccessfulResponse(
                    "500 Internal Server Error",
                    "Error en el servidor: " + e.getMessage(),
                    "/auth/login"
                ));
        }
    }
}
