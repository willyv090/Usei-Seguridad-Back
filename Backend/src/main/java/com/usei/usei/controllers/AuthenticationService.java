package com.usei.usei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.*;
import com.usei.usei.repositories.*;

import java.time.LocalDate;
import java.util.*;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioDAO usuarioDAO;
    
    @Autowired
    private EstudianteDAO estudianteDAO;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ContraseniaDAO contraseniaDAO;

    /**
     * Método principal de autenticación que maneja tanto Usuario como Estudiante
     */
    @Transactional
    public Map<String, Object> authenticate(String correo, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();
        
        // ========================================
        // INTENTAR LOGIN COMO USUARIO
        // ========================================
        Optional<Usuario> usuarioOpt = usuarioDAO.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            return authenticateUsuario(usuarioOpt.get(), passwordPlano);
        }
        
        // ========================================
        // INTENTAR LOGIN COMO ESTUDIANTE
        // ========================================
        Optional<Estudiante> estudianteOpt = estudianteDAO.findByCorreoInstitucional(correo);
        if (estudianteOpt.isPresent()) {
            return authenticateEstudiante(estudianteOpt.get(), passwordPlano);
        }
        
        // ========================================
        // NO SE ENCONTRÓ EL USUARIO
        // ========================================
        result.put("success", false);
        result.put("message", "No se encontró ninguna cuenta con ese correo electrónico.");
        return result;
    }
    
    /**
     * Autenticación para Usuario (usa tabla Contrasenia)
     */
    private Map<String, Object> authenticateUsuario(Usuario usuario, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();
        Contrasenia contrasenia = usuario.getContraseniaEntity();
        
        // Verificar que tenga contraseña configurada
        if (contrasenia == null) {
            result.put("success", false);
            result.put("message", "Usuario sin contraseña configurada");
            return result;
        }
        
        // Verificar si está bloqueado
        if (contrasenia.getIntentosRestantes() <= 0) {
            result.put("success", false);
            result.put("message", "Cuenta bloqueada por intentos fallidos. Contacte al administrador.");
            result.put("bloqueado", true);
            return result;
        }
        
        // Verificar contraseña
        if (!passwordEncoder.matches(passwordPlano, contrasenia.getContrasenia())) {
            // Decrementar intentos
            int restantes = Math.max(0, contrasenia.getIntentosRestantes() - 1);
            contrasenia.setIntentosRestantes(restantes);
            contraseniaDAO.save(contrasenia);
            
            result.put("success", false);
            result.put("message", "Credenciales incorrectas. Intentos restantes: " + restantes);
            result.put("intentosRestantes", restantes);
            return result;
        }
        
        // Verificar expiración de contraseña
        LocalDate fechaCreacion = contrasenia.getFechaCreacion();
        if (fechaCreacion != null && LocalDate.now().isAfter(fechaCreacion.plusDays(60))) {
            result.put("success", false);
            result.put("message", "Su contraseña ha expirado. Debe cambiarla.");
            result.put("expired", true);
            result.put("idUsuario", usuario.getIdUsuario());
            return result;
        }
        
        // Login exitoso - resetear intentos y actualizar último login
        contrasenia.setIntentosRestantes(3);
        contrasenia.setUltimoLog(LocalDate.now());
        contraseniaDAO.save(contrasenia);
        
        // Obtener accesos del rol
        List<String> accesos = parseAccesos(usuario.getRolEntity());
        
        result.put("success", true);
        result.put("tipo", "usuario");
        result.put("data", usuario);
        result.put("accesos", accesos);
        result.put("cambioContrasenia", usuario.getCambioContrasenia());
        return result;
    }
    
    /**
     * Autenticación para Estudiante (usa campo contrasena hasheado con BCrypt)
     */
    private Map<String, Object> authenticateEstudiante(Estudiante estudiante, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();
        String contrasenaHasheada = estudiante.getContrasena();
        
        // Verificar que tenga contraseña
        if (contrasenaHasheada == null || contrasenaHasheada.isEmpty()) {
            result.put("success", false);
            result.put("message", "Estudiante sin contraseña configurada");
            return result;
        }
        
        // Verificar contraseña
        // Si la contraseña NO está hasheada (empieza con texto plano), hashearla
        if (!contrasenaHasheada.startsWith("$2a$") && !contrasenaHasheada.startsWith("$2b$")) {
            // Es contraseña en texto plano, verificar directamente
            if (!contrasenaHasheada.equals(passwordPlano)) {
                result.put("success", false);
                result.put("message", "Credenciales incorrectas");
                return result;
            }
            // TODO: Aquí podrías hashear la contraseña para la próxima vez
        } else {
            // Es contraseña hasheada, verificar con BCrypt
            if (!passwordEncoder.matches(passwordPlano, contrasenaHasheada)) {
                result.put("success", false);
                result.put("message", "Credenciales incorrectas");
                return result;
            }
        }
        
        // Login exitoso
        // Estudiantes tienen acceso limitado predefinido
        List<String> accesos = Arrays.asList(
            "dashboard", 
            "encuestas", 
            "certificados", 
            "notificaciones",
            "perfil"
        );
        
        result.put("success", true);
        result.put("tipo", "estudiante");
        result.put("data", estudiante);
        result.put("accesos", accesos);
        return result;
    }
    
    /**
     * Parsea los accesos del rol desde formato CSV
     */
    private List<String> parseAccesos(Rol rol) {
        if (rol == null || rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Los accesos están guardados como CSV: "usuarios,roles,reportes"
        String[] accesosArray = rol.getAccesos().split(",");
        List<String> resultado = new ArrayList<>();
        
        for (String acceso : accesosArray) {
            String accesoLimpio = acceso.trim();
            if (!accesoLimpio.isEmpty()) {
                resultado.add(accesoLimpio);
            }
        }
        
        return resultado;
    }
    
    /**
     * Verifica si un rol tiene un acceso específico
     */
    public boolean tieneAcceso(Rol rol, String accesoRequerido) {
        List<String> accesos = parseAccesos(rol);
        return accesos.stream()
            .anyMatch(acceso -> acceso.equalsIgnoreCase(accesoRequerido));
    }
}