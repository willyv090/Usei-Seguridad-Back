package com.usei.usei.controllers;

import com.usei.usei.models.Contrasenia;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.ContraseniaDAO;
import com.usei.usei.repositories.HContraseniaDAO;
import com.usei.usei.util.PasswordPolicyUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityBL {

    private final UsuarioService usuarioService;
    private final ContraseniaDAO contraseniaDAO;
    private final HContraseniaDAO hContraseniaDAO;
    private final PasswordEncoder bcrypt;
    private final PasswordPolicyUtil passwordPolicyUtil;

    public SecurityBL(UsuarioService usuarioService,
                      ContraseniaDAO contraseniaDAO,
                      HContraseniaDAO hContraseniaDAO,
                      PasswordEncoder bcrypt,
                      PasswordPolicyUtil passwordPolicyUtil) {
        this.usuarioService = usuarioService;
        this.contraseniaDAO = contraseniaDAO;
        this.hContraseniaDAO = hContraseniaDAO;
        this.bcrypt = bcrypt;
        this.passwordPolicyUtil = passwordPolicyUtil;
    }

    @Transactional
    public LoginStatus login(String correo, String passwordPlano) {
        Optional<Usuario> ou = usuarioService.findByCorreo(correo);
        if (ou.isEmpty()) return LoginStatus.CREDENCIALES;

        Usuario u = ou.get();
        Contrasenia c = u.getContraseniaEntity();
        if (c == null) return LoginStatus.CREDENCIALES;
        if (c.getIntentosRestantes() <= 0) return LoginStatus.BLOQUEADO;

        boolean ok = bcrypt.matches(passwordPlano, c.getContrasenia());
        if (!ok) {
            int rest = Math.max(0, c.getIntentosRestantes() - 1);
            c.setIntentosRestantes(rest);
            contraseniaDAO.save(c);
            return (rest == 0) ? LoginStatus.BLOQUEADO : LoginStatus.CREDENCIALES;
        }

        c.setIntentosRestantes(passwordPolicyUtil.getMaxIntentos());
        c.setUltimoLog(LocalDate.now());
        contraseniaDAO.save(c);

        // Check if password has expired
        LocalDate fc = c.getFechaCreacion();
        if (fc != null && LocalDate.now().isAfter(fc.plusDays(passwordPolicyUtil.getExpiraDias()))) {
            System.out.println("Password expired for user: " + correo);
            return LoginStatus.EXPIRADA;
        }

        // Check if existing password complies with current security policies
        System.out.println("=== CHECKING POLICY COMPLIANCE FOR USER: " + correo + " ===");
        boolean complies = passwordPolicyUtil.existingPasswordCompliesWithCurrentPolicy(c);
        System.out.println("Policy compliance result: " + complies);
        
        if (!complies) {
            System.out.println("Password does not comply with current policies - forcing password change");
            System.out.println("User: " + correo + " (ID: " + u.getIdUsuario() + ")");
            // Mark user for mandatory password change
            u.setCambioContrasenia(true);
            usuarioService.save(u);
            System.out.println("User marked for password change, returning POLITICA_ACTUALIZADA");
            return LoginStatus.POLITICA_ACTUALIZADA;
        }

        System.out.println("Login successful for user: " + correo);
        return LoginStatus.OK;
    }

    @Transactional
    public PasswordChangeStatus changePassword(Long idUsuario, String nuevaPlano) {
        try {
            System.out.println("🔍 Starting password change for user ID: " + idUsuario);
            
            if (!passwordPolicyUtil.cumplePolitica(nuevaPlano)) {
                System.out.println("❌ Password does not meet policy requirements");
                return PasswordChangeStatus.POLITICA_NO_CUMPLIDA;
            }

            System.out.println("🔍 Looking up user with ID: " + idUsuario);
            Optional<Usuario> ou = usuarioService.findById(idUsuario);
            if (ou.isEmpty()) {
                System.out.println("❌ User not found with ID: " + idUsuario);
                return PasswordChangeStatus.USUARIO_SIN_CONTRASENIA;
            }

            Usuario u = ou.get();
            System.out.println("✅ User found: " + u.getCorreo());
            
            Contrasenia actual = u.getContraseniaEntity();
            if (actual == null) {
                System.out.println("❌ User has no password entity");
                return PasswordChangeStatus.USUARIO_SIN_CONTRASENIA;
            }

            System.out.println("✅ Current password entity found");

            // no igual a la actual
            if (bcrypt.matches(nuevaPlano, actual.getContrasenia())) {
                System.out.println("❌ New password is same as current password");
                return PasswordChangeStatus.REUTILIZACION_ULTIMA;
            }

            // Check password history to prevent reuse (with safe error handling)
            try {
                LocalDateTime desde = LocalDateTime.now().minusMonths(passwordPolicyUtil.getNoReuseMeses());
                List<String> hashes = hContraseniaDAO.findHashesSince(u.getIdUsuario(), desde);
                boolean reused = hashes.stream().anyMatch(h -> bcrypt.matches(nuevaPlano, h));
                if (reused) {
                    System.out.println("🚫 Password reuse detected for user: " + u.getIdUsuario());
                    return PasswordChangeStatus.REUTILIZACION_HISTORIAL;
                }
                System.out.println("✅ Password history check passed for user: " + u.getIdUsuario());
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not check password history: " + e.getMessage());
                // Continue with password change - don't block user due to history check failure
                // This ensures password changes work even if history table has issues
            }

            // Save current password to history before updating (in separate transaction)
            this.savePasswordToHistorySafely(actual, u.getIdUsuario());

            // actualizar nueva contraseña
            System.out.println("🔍 Updating password...");
            String nuevoHash = bcrypt.encode(nuevaPlano);
            actual.setContrasenia(nuevoHash);
            actual.setFechaCreacion(LocalDate.now());
            actual.setUltimoLog(LocalDate.now());
            actual.setLongitud(nuevaPlano.length());
            actual.setComplejidad(passwordPolicyUtil.calcularComplejidad(nuevaPlano));
            actual.setIntentosRestantes(passwordPolicyUtil.getMaxIntentos());
            contraseniaDAO.save(actual);
            System.out.println("✅ Password updated in database");

            u.setCambioContrasenia(false);
            usuarioService.save(u);
            System.out.println("✅ User cambio_contrasenia flag reset");

            System.out.println("✅ Password change completed successfully for user: " + u.getCorreo());
            return PasswordChangeStatus.CAMBIO_OK;
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in changePassword: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    @Transactional
    public Contrasenia crearPasswordInicial(String valorPlano) {
        String hash = bcrypt.encode(valorPlano);
        Contrasenia c = new Contrasenia();
        c.setContrasenia(hash);
        c.setFechaCreacion(LocalDate.now());
        c.setUltimoLog(LocalDate.now());
        c.setLongitud(passwordPolicyUtil.getMinLength());
        c.setComplejidad(passwordPolicyUtil.getComplejidad());
        c.setIntentosRestantes(passwordPolicyUtil.getMaxIntentos());
        return contraseniaDAO.save(c);
    }

    @Transactional
    public LoginStatus loginEstudiante(com.usei.usei.models.Estudiante estudiante, String passwordPlano) {
        if (estudiante == null) return LoginStatus.CREDENCIALES;
        
        // Check if account is blocked
        if (estudiante.getIntentosRestantes() <= 0) return LoginStatus.BLOQUEADO;

        // Validate password - Estudiante stores plain text password for now
        boolean ok = estudiante.getContrasena().equals(passwordPlano);
        
        if (!ok) {
            // Decrement attempts and save
            int rest = Math.max(0, estudiante.getIntentosRestantes() - 1);
            estudiante.setIntentosRestantes(rest);
            // Note: Estudiante will be saved by the calling service
            return (rest == 0) ? LoginStatus.BLOQUEADO : LoginStatus.CREDENCIALES;
        }

        // Successful login - reset attempts
        estudiante.setIntentosRestantes(passwordPolicyUtil.getMaxIntentos());
        // Note: Estudiante will be saved by the calling service
        return LoginStatus.OK;
    }

    @Transactional
    public void enforcePasswordPolicyUpdateForAllUsers() {
        System.out.println("Enforcing password policy update for all users...");
        
        // Get all users and mark them for password change
        Iterable<Usuario> allUsers = usuarioService.findAll();
        int updatedCount = 0;
        
        for (Usuario user : allUsers) {
            // Only mark users who currently have a password
            if (user.getContraseniaEntity() != null) {
                user.setCambioContrasenia(true);
                usuarioService.save(user);
                updatedCount++;
            }
        }
        
        System.out.println("Marked " + updatedCount + " users for mandatory password change due to policy update.");
    }

    /**
     * Save password to history in a separate transaction to avoid aborting the main transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePasswordToHistorySafely(Contrasenia contrasenia, Long userId) {
        try {
            // Use a unique timestamp to avoid primary key conflicts
            LocalDateTime txDate = LocalDateTime.now();
            
            // Check if this timestamp already exists for this id_pass and adjust if needed
            int attempts = 0;
            while (attempts < 10) { // Safety limit
                try {
                    hContraseniaDAO.insertHist(
                        contrasenia.getIdPass(),
                        contrasenia.getContrasenia(),
                        contrasenia.getFechaCreacion(),
                        contrasenia.getLongitud(),
                        contrasenia.getComplejidad(),
                        contrasenia.getIntentosRestantes(),
                        contrasenia.getUltimoLog(),
                        txDate,
                        userId
                    );
                    System.out.println("✅ Current password saved to history before update");
                    break; // Success, exit the retry loop
                } catch (Exception insertException) {
                    if (insertException.getMessage() != null && 
                        (insertException.getMessage().contains("duplicate key") || 
                         insertException.getMessage().contains("unique constraint"))) {
                        // Primary key conflict, adjust timestamp and retry
                        attempts++;
                        txDate = txDate.plusNanos(1000); // Add 1 microsecond
                        System.out.println("⚠️ PK conflict, retrying with adjusted timestamp, attempt: " + attempts);
                    } else {
                        // Different error, don't retry
                        throw insertException;
                    }
                }
            }
            
            if (attempts >= 10) {
                System.err.println("⚠️ Could not save password to history after 10 attempts");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not save current password to history: " + e.getMessage());
            e.printStackTrace();
            // This is in a separate transaction, so failure here won't affect the main password change
        }
    }
}
