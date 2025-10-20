package com.usei.usei.controllers;

import com.usei.usei.models.Contrasenia;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.ContraseniaDAO;
import com.usei.usei.repositories.HContraseniaDAO;
import com.usei.usei.util.PasswordPolicyUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.usei.usei.util.PasswordPolicyUtil.*;

@Service
public class SecurityBL {

    private final UsuarioService usuarioService;
    private final ContraseniaDAO contraseniaDAO;
    private final HContraseniaDAO hContraseniaDAO;
    private final PasswordEncoder bcrypt;

    public SecurityBL(UsuarioService usuarioService,
                      ContraseniaDAO contraseniaDAO,
                      HContraseniaDAO hContraseniaDAO,
                      PasswordEncoder bcrypt) {
        this.usuarioService = usuarioService;
        this.contraseniaDAO = contraseniaDAO;
        this.hContraseniaDAO = hContraseniaDAO;
        this.bcrypt = bcrypt;
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

        c.setIntentosRestantes(MAX_INTENTOS);
        c.setUltimoLog(LocalDate.now());
        contraseniaDAO.save(c);

        LocalDate fc = c.getFechaCreacion();
        if (fc != null && LocalDate.now().isAfter(fc.plusDays(EXPIRA_DIAS))) {
            return LoginStatus.EXPIRADA;
        }
        return LoginStatus.OK;
    }

    @Transactional
    public PasswordChangeStatus changePassword(Long idUsuario, String nuevaPlano) {
        if (!PasswordPolicyUtil.cumplePolitica(nuevaPlano)) {
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

        // no reutilizar en 12 meses: traer hashes del historial y verificar
        LocalDateTime desde = LocalDateTime.now().minusMonths(NO_REUSE_MESES);
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
        actual.setLongitud(MIN_LENGTH);
        actual.setComplejidad(COMPLEJIDAD);
        actual.setIntentosRestantes(MAX_INTENTOS);
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
        c.setLongitud(MIN_LENGTH);
        c.setComplejidad(COMPLEJIDAD);
        c.setIntentosRestantes(MAX_INTENTOS);
        return contraseniaDAO.save(c);
    }
}
