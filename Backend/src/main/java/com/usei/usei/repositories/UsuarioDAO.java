package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Usuario;

public interface UsuarioDAO extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoAndContrasenia(String correo, String contrasenia);

    Usuario findByCorreo(String correo);

    boolean existsByCi(String ci);

    // Necesario para bloquear el borrado de un rol en uso por algún usuario
    long countByRolEntity_IdRol(Long idRol);
}
