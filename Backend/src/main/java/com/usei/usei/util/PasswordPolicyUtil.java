package com.usei.usei.util;

import com.usei.usei.controllers.ConfiguracionSeguridadBL;
import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyUtil {

    private final ConfiguracionSeguridadBL configuracionService;
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

    public int getMinLength() {
        return getCurrentConfig().getMinLongitudContrasenia();
    }

    public int getMaxIntentos() {
        return getCurrentConfig().getMaxIntentosLogin();
    }

    public int getExpiraDias() {
        return getCurrentConfig().getDiasExpiracionContrasenia();
    }

    public int getNoReuseMeses() {
        return getCurrentConfig().getMesesNoReutilizar();
    }

    public int getComplejidad() {
        ConfiguracionSeguridad config = getCurrentConfig();
        int complejidad = 0;
        
        if (config.isRequerirMayusculas()) complejidad++;
        if (config.isRequerirMinusculas()) complejidad++;
        if (config.isRequerirNumeros()) complejidad++;
        if (config.isRequerirSimbolos()) complejidad++;
        
        return complejidad;
    }

    public int calcularComplejidad(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int complexity = 0;
        
        if (UPPER.matcher(password).find()) {
            complexity++; // Has uppercase
        }
        
        if (LOWER.matcher(password).find()) {
            complexity++; // Has lowercase
        }
        
        if (DIGIT.matcher(password).find()) {
            complexity++; // Has digits
        }
        
        if (SPECIAL.matcher(password).find()) {
            complexity++; // Has special characters
        }
        
        return complexity;
    }

    private ConfiguracionSeguridad getCurrentConfig() {
        try {
            return configuracionService.getCurrentConfigurationOrDefault();
        } catch (Exception e) {
            // Fallback to default configuration if database is not available
            return createDefaultConfig();
        }
    }

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

    public boolean existingPasswordCompliesWithCurrentPolicy(com.usei.usei.models.Contrasenia contrasenia) {
        if (contrasenia == null) {
            System.out.println("POLICY CHECK: Password is null - forcing update");
            return false;
        }
        
        ConfiguracionSeguridad config = getCurrentConfig();
        System.out.println("POLICY COMPLIANCE CHECK");
        
        // If password was created before policy was last modified, force update
        java.time.LocalDate passwordCreated = contrasenia.getFechaCreacion();
        java.time.LocalDateTime policyModified = config.getFechaModificacion();
        
        System.out.println("Password created on: " + passwordCreated);
        System.out.println("Policy last modified: " + policyModified);
        
        if (passwordCreated != null && policyModified != null) {
            // Convert password creation date to start of day for comparison
            java.time.LocalDateTime passwordCreatedDateTime = passwordCreated.atStartOfDay();
            
            // Check if password was created recently (today or within last hour of policy modification)
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean passwordCreatedToday = passwordCreated.equals(today);
            
            // Also check if password was created within 1 hour after policy modification
            // This handles cases where password is updated immediately after policy change
            java.time.LocalDateTime oneHourAfterPolicy = policyModified.plusHours(1);
            boolean passwordCreatedWithinBuffer = passwordCreatedDateTime.isAfter(policyModified) && 
                                                 passwordCreatedDateTime.isBefore(oneHourAfterPolicy);
            
            System.out.println("Password created today? " + passwordCreatedToday);
            System.out.println("Password created within buffer after policy? " + passwordCreatedWithinBuffer);
            
            if (passwordCreatedToday || passwordCreatedWithinBuffer) {
                System.out.println("Password was created/updated recently - considering it compliant with current policies");
            } else {
                // If password was created before the policy was last modified, force update
                if (passwordCreatedDateTime.isBefore(policyModified)) {
                    System.out.println("POLICY ENFORCEMENT: Password created BEFORE policy update!");
                    System.out.println("Password date: " + passwordCreatedDateTime + ", Policy modified: " + policyModified);
                    System.out.println("FORCING PASSWORD CHANGE - Security policies have been updated");
                    return false;
                } else {
                    System.out.println("Password created AFTER policy update - checking compliance...");
                }
            }
        }
        
        // Verify password still meets current requirements
        System.out.println("Checking current policy compliance...");
        System.out.println("Password length: " + contrasenia.getLongitud() + ", Required: " + config.getMinLongitudContrasenia());
        
        if (contrasenia.getLongitud() < config.getMinLongitudContrasenia()) {
            System.out.println("Password too short for current policy - forcing update");
            return false;
        }
        
        int currentComplexity = contrasenia.getComplejidad();
        int requiredComplexity = getComplejidad();
        
        System.out.println("Password complexity: " + currentComplexity + ", Required: " + requiredComplexity);
        
        if (currentComplexity < requiredComplexity) {
            System.out.println("Password complexity insufficient for current policy - forcing update");
            return false;
        }
        
        // Password is compliant with current policy
        System.out.println("Password complies with current policy - allowing login");
        return true;
    }
}
