package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Rol;

public interface RolDAO extends CrudRepository<Rol, Long> {

    Optional<Rol> findByNombreRol(String nombreRol);

    boolean existsByNombreRol(String nombreRol);

    // 🔹 Opcional: listar por estado (si lo usas en el futuro)
    Iterable<Rol> findByActivo(Boolean activo);
}
