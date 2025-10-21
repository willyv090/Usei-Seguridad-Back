package com.usei.usei.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.models.Rol;

@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "*") // 🔹 habilita llamadas desde el frontend
public class RolAPI {

    @Autowired
    private RolBL rolBL;

    // ✅ Crear rol completo
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Rol rol) {
        try {
            if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre del rol es obligatorio.");
            }

            Rol saved = rolBL.crearRolCompleto(rol);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear rol: " + ex.getMessage());
        }
    }

    // ✅ Listar roles
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(rolBL.listarRoles());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar roles: " + ex.getMessage());
        }
    }

    // ✅ Eliminar rol
    @DeleteMapping("/{idRol}")
    public ResponseEntity<?> eliminar(@PathVariable Long idRol) {
        try {
            rolBL.eliminarRol(idRol);
            return ResponseEntity.ok("Rol eliminado correctamente.");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar rol: " + ex.getMessage());
        }
    }

    // ✅ Cambiar estado (activar/desactivar rol)
    @PutMapping("/{idRol}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long idRol, @RequestBody Rol rolRequest) {
        try {
            boolean nuevoEstado = rolRequest.isActivo();
            Rol actualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        }
    }

    // ✅ Obtener rol por ID
    @GetMapping("/{idRol}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long idRol) {
        Rol rol = rolBL.obtenerRolPorId(idRol);
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
        }
        return ResponseEntity.ok(rol);
    }
     //Modificar rol
    @PutMapping("/{idRol}")
    public ResponseEntity<?> actualizarRol(@PathVariable Long idRol, @RequestBody Rol rolActualizado) {
        try {
            Rol actualizado = rolBL.actualizarRol(idRol, rolActualizado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el rol: " + ex.getMessage());
        }
    }

}
