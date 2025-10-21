package com.usei.usei.api;

import com.usei.usei.controllers.AuthenticationService;
import com.usei.usei.dto.request.UnifiedLoginRequest;
import com.usei.usei.dto.response.LoginResponseDTO;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.models.Estudiante;
import com.usei.usei.models.Usuario;
import com.usei.usei.util.TokenGenerator;

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

    /**
     * Endpoint unificado de login para Usuario y Estudiante
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UnifiedLoginRequest request) {
        try {
            // Validar que vengan los datos requeridos
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
            
            // Intentar autenticar
            Map<String, Object> authResult = authService.authenticate(
                request.getCorreo().trim(), 
                request.getContrasena()
            );
            
            Boolean success = (Boolean) authResult.get("success");
            
            // Si falla la autenticación
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
            
            // Login exitoso
            String tipo = (String) authResult.get("tipo");
            @SuppressWarnings("unchecked")
            List<String> accesos = (List<String>) authResult.get("accesos");
            
            Map<String, Object> data = new HashMap<>();
            String token = "";
            
            if ("usuario".equals(tipo)) {
                Usuario usuario = (Usuario) authResult.get("data");
                usuario.setContraseniaEntity(null); // No enviar la contraseña
                
                token = tokenGenerator.generateToken(
                    String.valueOf(usuario.getIdUsuario()),
                    "usuario",
                    usuario.getCorreo(),
                    60
                );
                
                data.put("id_usuario", usuario.getIdUsuario());
                data.put("tipo", "usuario");
                data.put("correo", usuario.getCorreo());
                data.put("nombre", usuario.getNombre());
                data.put("apellido", usuario.getApellido());
                data.put("rol", usuario.getRol());
                data.put("carrera", usuario.getCarrera());
                data.put("cambioContrasenia", authResult.get("cambioContrasenia"));
                
            } else if ("estudiante".equals(tipo)) {
                Estudiante estudiante = (Estudiante) authResult.get("data");
                estudiante.setContrasena(null); // No enviar la contraseña
                
                token = tokenGenerator.generateToken(
                    String.valueOf(estudiante.getIdEstudiante()),
                    "estudiante",
                    estudiante.getCorreoInstitucional(),
                    60
                );
                
                data.put("id_estudiante", estudiante.getIdEstudiante());
                data.put("tipo", "estudiante");
                data.put("correo", estudiante.getCorreoInstitucional());
                data.put("nombre", estudiante.getNombre());
                data.put("apellido", estudiante.getApellido());
                data.put("ci", estudiante.getCi());
                data.put("carrera", estudiante.getCarrera());
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