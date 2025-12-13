package com.usei.usei.controllers;

import com.usei.usei.models.LogRol;
import com.usei.usei.models.Rol;
import com.usei.usei.repositories.LogRolDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogRolService {

    private final LogRolDAO logRolDAO;

    public LogRolService(LogRolDAO logRolDAO) {
        this.logRolDAO = logRolDAO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(Rol rol, String motivo, String nivel, String mensaje, String detalle) {
        try {
            LogRol log = new LogRol(rol, motivo, nivel, mensaje, detalle);
            logRolDAO.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
