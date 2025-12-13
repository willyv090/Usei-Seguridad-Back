package com.usei.usei.api;

import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.controllers.LogUsuarioService;

import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;

import com.usei.usei.util.TokenGenerator;

@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "*")
public class RolAPI {

    @Autowired
    private RolBL rolBL;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogUsuarioService logUsuarioService;

    // =========================
    // HELPERS (token -> Usuario)
    // =========================

    private Usuario getUsuarioFromToken(String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) return null;

            // TokenGenerator ya soporta "Bearer "
            Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
            if (claims == null) return null;

            Object idObj = claims.getBody().get("id"); // tu token guarda "id"
            if (idObj == null) return null;

            Long userId;
            if (idObj instanceof Number n) userId = n.longValue();
            else userId = Long.parseLong(String.valueOf(idObj));

            return usuarioService.findById(userId).orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    // tu columna detalle es length=150 (en log usuario)
    private String safeDetalle(String s) {
        if (s == null) return "SIN_DETALLE";
        s = String.valueOf(s).trim();
        if (s.isBlank()) s = "SIN_DETALLE";
        return (s.length() > 150) ? s.substring(0, 150) : s;
    }

    /**
     * Registra log sin romper el endpoint.
     * Si no hay token válido, simplemente no registra.
     */
    private void tryLog(String authHeader, String motivo, String mensaje, String detalle) {
        try {
            Usuario authUser = getUsuarioFromToken(authHeader);
            if (authUser == null) return;

            logUsuarioService.registrarLog(
                    authUser,
                    "SEGURIDAD",
                    "ROL",
                    motivo,
                    "INFO",
                    mensaje,
                    safeDetalle(detalle)
            );
        } catch (Exception ignored) {
            // Nunca romper funcionalidad por un log
        }
    }

    // =========================
    // Helpers para detalle textual
    // =========================

    private List<String> parseAccesos(String accesos) {
        if (accesos == null || accesos.trim().isEmpty()) return List.of();
        return Arrays.stream(accesos.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private int countAccesos(String accesos) {
        return parseAccesos(accesos).size();
    }

    // ============
    // ENDPOINTS
    // ============

    // CREAR ROL
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Rol rol
    ) {
        try {
            if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre del rol es obligatorio.");
            }

            Rol saved = rolBL.crearRolCompleto(rol);

            String detalle = "Creó el rol \"" + saved.getNombreRol() + "\" (ID " + saved.getIdRol() + "). "
                    + "Accesos: " + countAccesos(saved.getAccesos()) + " permiso(s).";

            tryLog(
                    authHeader,
                    "CREAR_ROL",
                    "CREAR_ROL",
                    detalle
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (RuntimeException ex) {
            // (Opcional) log de error si quieres
            tryLog(authHeader, "CREAR_ROL_ERROR", "CREAR_ROL_ERROR", "No se pudo crear el rol: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(authHeader, "CREAR_ROL_ERROR", "CREAR_ROL_ERROR", "Error inesperado al crear rol: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear rol: " + ex.getMessage());
        }
    }

    // LISTAR ROLES  ✅ SIN LOG EN GET
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            Iterable<Rol> roles = rolBL.listarRoles();

            // ✅ NO LOGUEAR GET
            // tryLog(authHeader, "LISTAR_ROLES", "LISTAR_ROLES", "Listó roles del sistema");

            return ResponseEntity.ok(roles);

        } catch (Exception ex) {
            ex.printStackTrace();
            // Si quieres, puedes loguear errores internos (pero NO recomendado si también es GET)
            // tryLog(authHeader, "LISTAR_ROLES_ERROR", "LISTAR_ROLES_ERROR", "Error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar roles: " + ex.getMessage());
        }
    }

    // ELIMINAR ROL
    @DeleteMapping("/{idRol}")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            rolBL.eliminarRol(idRol);

            String detalle = "Eliminó el rol \"" + antes.getNombreRol() + "\" (ID " + idRol + ").";

            tryLog(
                    authHeader,
                    "ELIMINAR_ROL",
                    "ELIMINAR_ROL",
                    detalle
            );

            return ResponseEntity.ok("Rol eliminado correctamente.");

        } catch (RuntimeException ex) {
            tryLog(authHeader, "ELIMINAR_ROL_ERROR", "ELIMINAR_ROL_ERROR", "No se pudo eliminar el rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(authHeader, "ELIMINAR_ROL_ERROR", "ELIMINAR_ROL_ERROR", "Error inesperado al eliminar rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar rol: " + ex.getMessage());
        }
    }

    // CAMBIAR ESTADO (activar/desactivar rol)
    @PutMapping("/{idRol}/estado")
    public ResponseEntity<?> cambiarEstado(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol,
            @RequestBody Rol rolRequest
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            boolean estadoAntes = Boolean.TRUE.equals(antes.getActivo());
            boolean nuevoEstado = rolRequest.isActivo();

            Rol actualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);

            String detalle = "Cambió el estado del rol \"" + actualizado.getNombreRol() + "\" (ID " + idRol + "): "
                    + (estadoAntes ? "Activo" : "Inactivo") + " → " + (nuevoEstado ? "Activo" : "Inactivo") + ".";

            tryLog(
                    authHeader,
                    "CAMBIO_ESTADO_ROL",
                    "CAMBIO_ESTADO_ROL",
                    detalle
            );

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException ex) {
            tryLog(authHeader, "CAMBIO_ESTADO_ROL_ERROR", "CAMBIO_ESTADO_ROL_ERROR", "No se pudo cambiar estado del rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(authHeader, "CAMBIO_ESTADO_ROL_ERROR", "CAMBIO_ESTADO_ROL_ERROR", "Error inesperado al cambiar estado del rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        }
    }

    // OBTENER ROL POR ID ✅ SIN LOG EN GET
    @GetMapping("/{idRol}")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol
    ) {
        Rol rol = rolBL.obtenerRolPorId(idRol);
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
        }

        // ✅ NO LOGUEAR GET
        // tryLog(authHeader, "OBTENER_ROL", "OBTENER_ROL", "Consultó rol id=" + idRol);

        return ResponseEntity.ok(rol);
    }

    // ACTUALIZAR ROL
    @PutMapping("/{idRol}")
    public ResponseEntity<?> actualizarRol(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol,
            @RequestBody Rol rolActualizado
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            String nombreAntes = (antes.getNombreRol() == null) ? "" : antes.getNombreRol();
            boolean activoAntes = Boolean.TRUE.equals(antes.getActivo());
            String accesosAntes = (antes.getAccesos() == null) ? "" : antes.getAccesos();

            Rol actualizado = rolBL.actualizarRol(idRol, rolActualizado);

            String nombreNuevo = (actualizado.getNombreRol() == null) ? "" : actualizado.getNombreRol();
            boolean activoNuevo = Boolean.TRUE.equals(actualizado.getActivo());
            String accesosNuevo = (actualizado.getAccesos() == null) ? "" : actualizado.getAccesos();

            int cantAntes = countAccesos(accesosAntes);
            int cantNuevo = countAccesos(accesosNuevo);

            StringBuilder detalle = new StringBuilder();
            detalle.append("Actualizó el rol \"")
                    .append(nombreAntes.isBlank() ? "SIN_NOMBRE" : nombreAntes)
                    .append("\" (ID ")
                    .append(idRol)
                    .append("). ");

            boolean cambioAlgo = false;

            if (!nombreNuevo.equals(nombreAntes) && !nombreNuevo.isBlank()) {
                detalle.append("Nombre: \"").append(nombreAntes).append("\" → \"").append(nombreNuevo).append("\". ");
                cambioAlgo = true;
            }

            if (activoAntes != activoNuevo) {
                detalle.append("Estado: ").append(activoAntes ? "Activo" : "Inactivo")
                        .append(" → ").append(activoNuevo ? "Activo" : "Inactivo").append(". ");
                cambioAlgo = true;
            }

            if (cantAntes != cantNuevo) {
                detalle.append("Accesos: ").append(cantAntes).append(" → ").append(cantNuevo).append(" permiso(s). ");
                cambioAlgo = true;
            }

            if (!cambioAlgo) {
                detalle.append("No se detectaron cambios relevantes.");
            }

            tryLog(
                    authHeader,
                    "ACTUALIZAR_ROL",
                    "ACTUALIZAR_ROL",
                    detalle.toString()
            );

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException ex) {
            tryLog(authHeader, "ACTUALIZAR_ROL_ERROR", "ACTUALIZAR_ROL_ERROR", "No se pudo actualizar el rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(authHeader, "ACTUALIZAR_ROL_ERROR", "ACTUALIZAR_ROL_ERROR", "Error inesperado al actualizar el rol ID " + idRol + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el rol: " + ex.getMessage());
        }
    }

    // VERIFICAR ACCESO (normalmente se usa sin token desde el frontend)
    @PostMapping("/verificar-acceso")
    public ResponseEntity<?> verificarAcceso(@RequestBody Map<String, Object> request) {
        try {
            Long idRol = Long.parseLong(request.get("idRol").toString());
            String accesoRequerido = request.get("acceso").toString();

            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "tieneAcceso", false,
                                "error", "Rol no encontrado"
                        ));
            }

            if (rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
                return ResponseEntity.ok(Map.of("tieneAcceso", false));
            }

            String[] accesos = rol.getAccesos().split(",");
            boolean tieneAcceso = Arrays.stream(accesos)
                    .map(String::trim)
                    .anyMatch(a -> a.equalsIgnoreCase(accesoRequerido));

            return ResponseEntity.ok(Map.of(
                    "tieneAcceso", tieneAcceso,
                    "accesosDisponibles", Arrays.asList(accesos)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al verificar acceso: " + e.getMessage()
                    ));
        }
    }

    // OBTENER ACCESOS (GET) -> no se registra log
    @GetMapping("/{idRol}/accesos")
    public ResponseEntity<?> obtenerAccesos(@PathVariable Long idRol) {
        try {
            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Rol no encontrado"));
            }

            if (rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
                return ResponseEntity.ok(Map.of("accesos", new String[0]));
            }

            String[] accesos = rol.getAccesos().split(",");
            return ResponseEntity.ok(Map.of(
                    "idRol", idRol,
                    "nombreRol", rol.getNombreRol(),
                    "accesos", Arrays.stream(accesos).map(String::trim).toArray()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener accesos: " + e.getMessage()));
        }
    }
}
