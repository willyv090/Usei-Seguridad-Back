package com.usei.usei.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.models.Rol;

@RestController
@RequestMapping("/rol")
public class RolAPI {

    @Autowired private RolBL rolBL;

    static class CrearRolRequest { public String nombreRol; }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearRolRequest req) {
        try {
            Rol saved = rolBL.crearRol(req.nombreRol);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{idRol}")
    public ResponseEntity<?> eliminar(@PathVariable Long idRol) {
        try {
            rolBL.eliminarRol(idRol);
            return ResponseEntity.ok("Rol eliminado");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
