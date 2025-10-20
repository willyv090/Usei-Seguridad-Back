package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Usuario;

public interface UsuarioDAO extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo); 


    Optional<Usuario> findByCi(String ci);

    boolean existsByCi(String ci);

    long countByRolEntity_IdRol(Long idRol);
}
