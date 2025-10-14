package com.usei.usei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.Rol;
import com.usei.usei.repositories.RolDAO;
import com.usei.usei.repositories.UsuarioDAO;

@Service
public class RolBL {

    @Autowired private RolDAO rolDAO;
    @Autowired private UsuarioDAO usuarioDAO;

    @Transactional
    public Rol crearRol(String nombreRol) {
        String limpio = nombreRol == null ? "" : nombreRol.trim();
        if (limpio.isEmpty()) {
            throw new RuntimeException("El nombre del rol es obligatorio.");
        }
        rolDAO.findByNombreRol(limpio).ifPresent(r -> {
            throw new RuntimeException("El rol ya existe: " + limpio);
        });
        Rol r = new Rol(limpio);
        return rolDAO.save(r);
    }

    @Transactional
    public void eliminarRol(Long idRol) {
        if (idRol == null) throw new RuntimeException("idRol es obligatorio.");
        long enUso = usuarioDAO.countByRolEntity_IdRol(idRol);
        if (enUso > 0) {
            throw new RuntimeException("No se puede eliminar: rol asignado a " + enUso + " usuario(s).");
        }
        if (!rolDAO.existsById(idRol)) {
            throw new RuntimeException("Rol no encontrado: " + idRol);
        }
        rolDAO.deleteById(idRol);
    }
}
