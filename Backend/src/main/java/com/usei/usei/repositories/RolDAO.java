package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Rol;

public interface RolDAO extends CrudRepository<Rol, Long> {
    Optional<Rol> findByNombreRol(String nombreRol);

    // opcional: para validar duplicados si lo necesitas
    boolean existsByNombreRol(String nombreRol);
}
