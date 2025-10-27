package com.usei.usei.api;

import com.usei.usei.controllers.ConfiguracionSeguridadService;
import com.usei.usei.dto.SuccessfulResponse;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configuracion-seguridad")
public class ConfiguracionSeguridadAPI {

    private final ConfiguracionSeguridadService configuracionService;

    public ConfiguracionSeguridadAPI(ConfiguracionSeguridadService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("timestamp", java.time.LocalDateTime.now());
        
        SuccessfulResponse response = new SuccessfulResponse(
            "200 OK",
            "Backend is reachable",
            null,
            0,
            data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentConfiguration() {
        try {
            // Try to get active configuration first
            Optional<ConfiguracionSeguridad> activeConfig = configuracionService.getActiveConfiguration();
            
            ConfiguracionSeguridad config;
            if (activeConfig.isPresent()) {
                config = activeConfig.get();
            } else {
                // Create a hardcoded default configuration if database is not accessible
                config = new ConfiguracionSeguridad(
                    12,   // min password length
                    3,    // max login attempts
                    60,   // password expiry days
                    12,   // months no reuse
                    true, // require uppercase
                    true, // require lowercase
                    true, // require numbers
                    true, // require symbols
                    1L    // default user
                );
                config.setIdConfig(1L);
                config.setFechaModificacion(java.time.LocalDateTime.now());
                config.setActiva(true);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("configuracion", config);
            
            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Configuración de seguridad obtenida exitosamente",
                null,
                0,
                data
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return a minimal working configuration even if everything fails
            ConfiguracionSeguridad fallbackConfig = new ConfiguracionSeguridad(
                12, 3, 60, 12, true, true, true, true, 1L
            );
            fallbackConfig.setIdConfig(1L);
            fallbackConfig.setFechaModificacion(java.time.LocalDateTime.now());
            fallbackConfig.setActiva(true);
            
            Map<String, Object> data = new HashMap<>();
            data.put("configuracion", fallbackConfig);
            
            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Configuración por defecto (error de base de datos: " + e.getMessage() + ")",
                null,
                0,
                data
            );
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listAllConfigurations() {
        try {
            java.util.List<ConfiguracionSeguridad> all = configuracionService.findAll();

            java.util.Map<String,Object> data = new java.util.HashMap<>();
            data.put("configuraciones", all);

            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Listado de configuraciones obtenido",
                null,
                all.size(),
                data
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return empty list if database fails
            java.util.List<ConfiguracionSeguridad> emptyList = new java.util.ArrayList<>();
            java.util.Map<String,Object> data = new java.util.HashMap<>();
            data.put("configuraciones", emptyList);
            
            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Error de base de datos, lista vacía: " + e.getMessage(),
                null,
                0,
                data
            );
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateConfiguration(@RequestBody ConfiguracionSeguridad newConfig,
                                                @RequestParam Long userId) {
        try {
            System.out.println("=== CONFIGURATION UPDATE REQUEST ===");
            System.out.println("userId: " + userId);
            System.out.println("New config - Min Length: " + newConfig.getMinLongitudContrasenia());
            System.out.println("New config - Max Attempts: " + newConfig.getMaxIntentosLogin());
            System.out.println("New config - Require Upper: " + newConfig.isRequerirMayusculas());
            System.out.println("New config - Require Lower: " + newConfig.isRequerirMinusculas());
            System.out.println("New config - Require Numbers: " + newConfig.isRequerirNumeros());
            System.out.println("New config - Require Symbols: " + newConfig.isRequerirSimbolos());
            
            // TODO: Add authorization check - only 'Seguridad' role should access this
            // You can implement this using @PreAuthorize("hasRole('Seguridad')") or manual check
            
            // Validate configuration values
            if (!isValidConfiguration(newConfig)) {
                System.err.println("❌ Configuration validation failed");
                UnsuccessfulResponse response = new UnsuccessfulResponse(
                    "400 Bad Request",
                    "Configuración inválida. Verifique los valores ingresados.",
                    "/configuracion-seguridad"
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            System.out.println("🔧 Calling configuracionService.updateConfiguration...");
            ConfiguracionSeguridad updatedConfig = configuracionService.updateConfiguration(newConfig, userId);
            System.out.println("🔧 Configuration updated successfully!");
            System.out.println("🔧 Updated config ID: " + updatedConfig.getIdConfig());
            
            Map<String, Object> data = new HashMap<>();
            data.put("configuracion", updatedConfig);
            
            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Configuración de seguridad actualizada exitosamente",
                null,
                0,
                data
            );
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error updating configuration: " + e.getMessage());
            e.printStackTrace();
            // Still return success but indicate database issue
            Map<String, Object> data = new HashMap<>();
            data.put("configuracion", newConfig);
            data.put("warning", "Configuración no persistida en base de datos: " + e.getMessage());
            
            SuccessfulResponse response = new SuccessfulResponse(
                "200 OK",
                "Configuración recibida pero no guardada (error de BD)",
                null,
                0,
                data
            );
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getConfigurationById(@PathVariable Long id) {
        try {
            Optional<ConfiguracionSeguridad> config = configuracionService.findById(id);
            
            if (config.isPresent()) {
                Map<String, Object> data = new HashMap<>();
                data.put("configuracion", config.get());
                
                SuccessfulResponse response = new SuccessfulResponse(
                    "200 OK",
                    "Configuración encontrada",
                    null,
                    0,
                    data
                );
                return ResponseEntity.ok(response);
            } else {
                UnsuccessfulResponse response = new UnsuccessfulResponse(
                    "404 Not Found",
                    "Configuración no encontrada con ID: " + id,
                    "/configuracion-seguridad/" + id
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            UnsuccessfulResponse response = new UnsuccessfulResponse(
                "500 Internal Server Error",
                "Error al obtener configuración: " + e.getMessage(),
                "/configuracion-seguridad/" + id
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isValidConfiguration(ConfiguracionSeguridad config) {
        // Minimum password length should be between 6 and 50 characters
        if (config.getMinLongitudContrasenia() < 6 || config.getMinLongitudContrasenia() > 50) {
            return false;
        }
        
        // Maximum login attempts should be between 1 and 10
        if (config.getMaxIntentosLogin() < 1 || config.getMaxIntentosLogin() > 10) {
            return false;
        }
        
        // Password expiry should be between 30 and 365 days
        if (config.getDiasExpiracionContrasenia() < 30 || config.getDiasExpiracionContrasenia() > 365) {
            return false;
        }
        
        // No reuse period should be between 1 and 24 months
        if (config.getMesesNoReutilizar() < 1 || config.getMesesNoReutilizar() > 24) {
            return false;
        }
        
        return true;
    }
}