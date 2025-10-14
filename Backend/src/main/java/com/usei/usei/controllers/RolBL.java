package com.usei.usei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.usei.usei.models.Rol;
import com.usei.usei.repositories.RolDAO;

@Service
public class RolBL {

    @Autowired
    private RolDAO rolDAO;

    @Transactional
    public Rol crearRol(String nombreRol) {
        Rol r = new Rol(nombreRol);
        return rolDAO.save(r);
    }

    // ✅ nuevo método con activo + accesos
    @Transactional
    public Rol crearRolCompleto(Rol rol) {
        if (rol.getNombreRol() == null || rol.getNombreRol().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio.");
        }
        if (rolDAO.existsByNombreRol(rol.getNombreRol().trim())) {
            throw new RuntimeException("Ya existe un rol con ese nombre.");
        }
        return rolDAO.save(rol);
    }

    @Transactional
    public void eliminarRol(Long idRol) {
        Rol rol = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        rolDAO.delete(rol);
    }

    public Iterable<Rol> listarRoles() {
        return rolDAO.findAll();
    }

}
