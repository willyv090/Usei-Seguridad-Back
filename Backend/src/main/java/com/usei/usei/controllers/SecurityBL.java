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

        // int primitivo: no comparar con null
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

        LocalDate fc = c.getFechaCreacion();
        if (fc != null && LocalDate.now().isAfter(fc.plusDays(passwordPolicyUtil.getExpiraDias()))) {
            return LoginStatus.EXPIRADA;
        }
        return LoginStatus.OK;
    }

    @Transactional
    public PasswordChangeStatus changePassword(Long idUsuario, String nuevaPlano) {
        if (!passwordPolicyUtil.cumplePolitica(nuevaPlano)) {
            return PasswordChangeStatus.POLITICA_NO_CUMPLIDA;
        }

        Optional<Usuario> ou = usuarioService.findById(idUsuario);
        if (ou.isEmpty()) return PasswordChangeStatus.USUARIO_SIN_CONTRASENIA;

        Usuario u = ou.get();
        Contrasenia actual = u.getContraseniaEntity();
        if (actual == null) return PasswordChangeStatus.USUARIO_SIN_CONTRASENIA;

        // no igual a la actual
        if (bcrypt.matches(nuevaPlano, actual.getContrasenia())) {
            return PasswordChangeStatus.REUTILIZACION_ULTIMA;
        }

        // no reutilizar en X meses: traer hashes del historial y verificar
        LocalDateTime desde = LocalDateTime.now().minusMonths(passwordPolicyUtil.getNoReuseMeses());
        List<String> hashes = hContraseniaDAO.findHashesSince(u.getIdUsuario(), desde);
        boolean reused = hashes.stream().anyMatch(h -> bcrypt.matches(nuevaPlano, h));
        if (reused) return PasswordChangeStatus.REUTILIZACION_HISTORIAL;

        // guardar la contraseña actual en H_Contrasenia
        hContraseniaDAO.insertHist(
            actual.getIdPass(),
            actual.getContrasenia(),
            actual.getFechaCreacion(),
            actual.getLongitud(),
            actual.getComplejidad(),
            actual.getIntentosRestantes(),
            actual.getUltimoLog(),
            LocalDateTime.now(),
            u.getIdUsuario()
        );

        // actualizar nueva contraseña
        String nuevoHash = bcrypt.encode(nuevaPlano);
        actual.setContrasenia(nuevoHash);
        actual.setFechaCreacion(LocalDate.now());
        actual.setUltimoLog(LocalDate.now());
        actual.setLongitud(passwordPolicyUtil.getMinLength());
        actual.setComplejidad(passwordPolicyUtil.getComplejidad());
        actual.setIntentosRestantes(passwordPolicyUtil.getMaxIntentos());
        contraseniaDAO.save(actual);

        u.setCambioContrasenia(false);
        usuarioService.save(u);

        return PasswordChangeStatus.CAMBIO_OK;
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

    /**
     * Login method for Estudiante entities that have their own intentos_restantes field
     * This provides consistent attempt tracking behavior across all user types
     */
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
}
