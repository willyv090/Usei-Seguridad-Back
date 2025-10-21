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

    public Rol cambiarEstadoRol(Long idRol, boolean nuevoEstado) {
        Rol rol = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + idRol));

        rol.setActivo(nuevoEstado);
        return rolDAO.save(rol);
    }

    //Obtener el rol mediante su ID
    public Rol obtenerRolPorId(Long idRol) {
        return rolDAO.findById(idRol).orElse(null);
    }

    // 🔹 Obtener rol por nombre
    @Transactional(readOnly = true)
    public Rol obtenerRolPorNombre(String nombreRol) {
        if (nombreRol == null || nombreRol.isBlank()) return null;
        return rolDAO.findByNombreRol(nombreRol).orElse(null);
    }

    @Transactional
    public Rol actualizarRol(Long idRol, Rol rolActualizado) {
        Rol existente = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + idRol));

        // Evita duplicados
        if (!existente.getNombreRol().equalsIgnoreCase(rolActualizado.getNombreRol())
                && rolDAO.existsByNombreRol(rolActualizado.getNombreRol().trim())) {
            throw new RuntimeException("Ya existe un rol con ese nombre.");
        }

        // Actualiza los campos modificables
        existente.setNombreRol(rolActualizado.getNombreRol());
        existente.setActivo(rolActualizado.getActivo());
        existente.setAccesos(rolActualizado.getAccesos());

        return rolDAO.save(existente);
    }



}
