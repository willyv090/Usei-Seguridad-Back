package com.usei.usei.controllers;

import com.usei.usei.models.LogUsuario;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.LogUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogUsuarioService {

    private final LogUsuarioDAO logUsuarioDAO;

    public LogUsuarioService(LogUsuarioDAO logUsuarioDAO) {
        this.logUsuarioDAO = logUsuarioDAO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(
            Usuario usuario,
            String tipoLog,       // SEGURIDAD, SISTEMA, etc.
            String modulo,        // LOGS, AUTH, USUARIOS, etc.
            String motivo,        // VER_LOGS, LOGIN_OK...
            String nivel,         // INFO, WARN, ERROR...
            String mensaje,       // texto legible
            String detalle        // texto/JSON extra
    ) {
        try {
            LogUsuario log = new LogUsuario(
                    usuario,
                    tipoLog,
                    modulo,
                    motivo,
                    nivel,
                    mensaje,
                    detalle
            );
            logUsuarioDAO.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // compatibilidad por si ya llamabas a este método en otros lados
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogSeguridad(
            Usuario usuario,
            String motivo,
            String nivel,
            String mensaje,
            String detalle
    ) {
        registrarLog(usuario, "SEGURIDAD", "AUTH", motivo, nivel, mensaje, detalle);
    }
}
