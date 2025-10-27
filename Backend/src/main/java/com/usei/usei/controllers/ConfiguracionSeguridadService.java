package com.usei.usei.controllers;

import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.Optional;

public interface ConfiguracionSeguridadService {

    Optional<ConfiguracionSeguridad> getActiveConfiguration();
    ConfiguracionSeguridad updateConfiguration(ConfiguracionSeguridad newConfig, Long userId);
    Optional<ConfiguracionSeguridad> findById(Long id);
    java.util.List<ConfiguracionSeguridad> findAll();
    boolean hasActiveConfiguration();
}