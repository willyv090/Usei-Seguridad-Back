package com.usei.usei.util;

import com.usei.usei.controllers.ConfiguracionSeguridadBL;
import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Dynamic password policy utility that reads configuration from database
 * Converted from static utility to Spring component to allow dependency injection
 */
@Component
public class PasswordPolicyUtil {

    private final ConfiguracionSeguridadBL configuracionService;

    // Fallback constants in case database is not available
    public static final int DEFAULT_MIN_LENGTH = 12;
    public static final int DEFAULT_COMPLEJIDAD = 4;
    public static final int DEFAULT_MAX_INTENTOS = 3;
    public static final int DEFAULT_EXPIRA_DIAS = 60;
    public static final int DEFAULT_NO_REUSE_MESES = 12;

    private static final Pattern UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern LOWER   = Pattern.compile("[a-z]");
    private static final Pattern DIGIT   = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public PasswordPolicyUtil(ConfiguracionSeguridadBL configuracionService) {
        this.configuracionService = configuracionService;
    }

    /**
     * Check if password meets current policy requirements
     */
    public boolean cumplePolitica(String plain) {
        ConfiguracionSeguridad config = getCurrentConfig();
        
        if (plain == null || plain.length() < config.getMinLongitudContrasenia()) {
            return false;
        }
        
        if (config.isRequerirMayusculas() && !UPPER.matcher(plain).find()) {
            return false;
        }
        
        if (config.isRequerirMinusculas() && !LOWER.matcher(plain).find()) {
            return false;
        }
        
        if (config.isRequerirNumeros() && !DIGIT.matcher(plain).find()) {
            return false;
        }
        
        if (config.isRequerirSimbolos() && !SPECIAL.matcher(plain).find()) {
            return false;
        }
        
        return true;
    }

    /**
     * Get current minimum password length
     */
    public int getMinLength() {
        return getCurrentConfig().getMinLongitudContrasenia();
    }

    /**
     * Get current maximum login attempts
     */
    public int getMaxIntentos() {
        return getCurrentConfig().getMaxIntentosLogin();
    }

    /**
     * Get password expiry days
     */
    public int getExpiraDias() {
        return getCurrentConfig().getDiasExpiracionContrasenia();
    }

    /**
     * Get months before password can be reused
     */
    public int getNoReuseMeses() {
        return getCurrentConfig().getMesesNoReutilizar();
    }

    /**
     * Calculate complexity score based on current policy
     */
    public int getComplejidad() {
        ConfiguracionSeguridad config = getCurrentConfig();
        int complejidad = 0;
        
        if (config.isRequerirMayusculas()) complejidad++;
        if (config.isRequerirMinusculas()) complejidad++;
        if (config.isRequerirNumeros()) complejidad++;
        if (config.isRequerirSimbolos()) complejidad++;
        
        return complejidad;
    }

    /**
     * Get current configuration, with fallback to defaults
     */
    private ConfiguracionSeguridad getCurrentConfig() {
        try {
            return configuracionService.getCurrentConfigurationOrDefault();
        } catch (Exception e) {
            // Fallback to default configuration if database is not available
            return createDefaultConfig();
        }
    }

    /**
     * Create a default configuration for fallback scenarios
     */
    private ConfiguracionSeguridad createDefaultConfig() {
        return new ConfiguracionSeguridad(
            DEFAULT_MIN_LENGTH,
            DEFAULT_MAX_INTENTOS, 
            DEFAULT_EXPIRA_DIAS,
            DEFAULT_NO_REUSE_MESES,
            true, // require uppercase
            true, // require lowercase
            true, // require numbers
            true, // require symbols
            1L    // system user
        );
    }

    // Static methods for backward compatibility (deprecated - use instance methods)
    
    @Deprecated
    public static final int MIN_LENGTH = DEFAULT_MIN_LENGTH;
    @Deprecated 
    public static final int COMPLEJIDAD = DEFAULT_COMPLEJIDAD;
    @Deprecated
    public static final int MAX_INTENTOS = DEFAULT_MAX_INTENTOS;
    @Deprecated
    public static final int EXPIRA_DIAS = DEFAULT_EXPIRA_DIAS;
    @Deprecated
    public static final int NO_REUSE_MESES = DEFAULT_NO_REUSE_MESES;
}
