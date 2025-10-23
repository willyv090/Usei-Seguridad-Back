package com.usei.usei.controllers;

import java.time.LocalDate;
import java.util.Optional;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.Contrasenia;
import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.ContraseniaDAO;
import com.usei.usei.repositories.RolDAO;
import com.usei.usei.repositories.UsuarioDAO;
import com.usei.usei.util.PasswordPolicyUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UsuarioBL implements UsuarioService {

    @Autowired private UsuarioDAO usuarioDAO;
    @Autowired private RolDAO rolDAO;
    @Autowired private ContraseniaDAO contraseniaDAO;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder; // BCrypt
    private String codigoVerificacion;

    @Autowired
    public UsuarioBL(UsuarioDAO usuarioDAO,
                     JavaMailSender mailSender,
                     PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /* ==========================
       CRUD BÁSICO
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Iterable<Usuario> findAll() { return usuarioDAO.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) { return usuarioDAO.findById(id); }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        // Verificación de no duplicados de CI y Correos institucionales al crear usuario
        Optional<Usuario> existentePorCi = usuarioDAO.findByCi(usuario.getCi());
        if (existentePorCi.isPresent() && !existentePorCi.get().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el CI " + usuario.getCi());
        }

        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
            Optional<Usuario> existentePorCorreo = usuarioDAO.findByCorreo(usuario.getCorreo());
            if (existentePorCorreo.isPresent() && !existentePorCorreo.get().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new RuntimeException("Ya existe un usuario con el correo " + usuario.getCorreo());
            }
        }


        // Generar contraseña por defecto si no tiene aún
        if (usuario.getContraseniaEntity() == null) {
            final String contraseniaGenerada = buildInitialPassword(
                    nullSafe(usuario.getNombre()), nullSafe(usuario.getApellido()), nullSafe(usuario.getCi())
            );

            // Hash (nunca guardes texto plano)
            String hash = passwordEncoder.encode(contraseniaGenerada);

            // Crear entidad Contrasenia con política
            Contrasenia contrasenia = new Contrasenia();
            contrasenia.setContrasenia(hash);
            contrasenia.setFechaCreacion(LocalDate.now());
            contrasenia.setUltimoLog(LocalDate.now());
            contrasenia.setLongitud(Math.max(PasswordPolicyUtil.MIN_LENGTH, contraseniaGenerada.length()));
            contrasenia.setComplejidad(PasswordPolicyUtil.COMPLEJIDAD); // 4: mayus/minus/num/especial
            contrasenia.setIntentosRestantes(PasswordPolicyUtil.MAX_INTENTOS);

            contrasenia = contraseniaDAO.save(contrasenia);
            usuario.setContraseniaEntity(contrasenia);

            // Forzar cambio de contraseña al primer login
            usuario.setCambioContrasenia(true);

            // Enviar correo de notificación (si hay correo)
            if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
                try {
                    String cuerpo = """
                    Estimado/a %s %s,
                    
                    Su cuenta ha sido creada exitosamente para el Sistema USEI.
                    
                    Sus credenciales iniciales son:
                    Usuario: %s
                    Contraseña: %s
                    
                    Por seguridad, deberá cambiar su contraseña en su primer inicio de sesión.
                    
                    Saludos cordiales,
                    Equipo USEI
                    Universidad Católica Boliviana "San Pablo"
                    """.formatted(
                            usuario.getNombre(),
                            usuario.getApellido(),
                            usuario.getCi(),
                            contraseniaGenerada
                    );
                    enviarCorreo(usuario.getCorreo(), "Credenciales de acceso - Sistema USEI", cuerpo);
                } catch (Exception e) {
                    System.err.println("Error al enviar correo a " + usuario.getCorreo() + ": " + e.getMessage());
                }
            }
        }

        return usuarioDAO.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) { usuarioDAO.deleteById(id); }

    @Override
    @Transactional
    public Usuario update(Usuario usuario, Long id) {
        Usuario u = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        u.setNombre(usuario.getNombre());
        u.setApellido(usuario.getApellido());
        u.setTelefono(usuario.getTelefono());
        u.setCorreo(usuario.getCorreo());
        u.setCarrera(usuario.getCarrera());
        u.setCi(usuario.getCi());
        u.setCambioContrasenia(usuario.getCambioContrasenia());
        if (usuario.getRolEntity() != null) {
            u.setRolEntity(usuario.getRolEntity());
            u.setRol(usuario.getRolEntity().getNombreRol());
        }

        return usuarioDAO.save(u);
    }

    /* ==========================
       LOGIN (plano; recomendado usar SecurityBL.login() en API)
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String contraseniaIngresada) {
        Optional<Usuario> ou = usuarioDAO.findByCorreo(correo);
        Usuario usuario = ou.orElse(null);
        if (usuario == null) return Optional.empty();

        Contrasenia pass = usuario.getContraseniaEntity();
        if (pass != null && passwordEncoder.matches(contraseniaIngresada, pass.getContrasenia())) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /* ==========================
       BÚSQUEDA POR CORREO (lo pide SecurityBL)
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioDAO.findByCorreo(correo);
    }

    /* ==========================
       CÓDIGO DE VERIFICACIÓN EMAIL
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Long findByMail(String correo) {
        return usuarioDAO.findByCorreo(correo)
                .map(Usuario::getIdUsuario)
                .orElse(0L);
    }

    @Override
    public void enviarCodigoVerificacion(String correo) throws MessagingException {
        Usuario usuario = usuarioDAO.findByCorreo(correo).orElse(null);
        if (usuario == null)
            throw new MessagingException("No existe usuario con correo: " + correo);

        this.codigoVerificacion = generarCodigoVerificacion();
        String cuerpo = "Estimado " + usuario.getNombre()
                + ", su código de verificación es: " + this.codigoVerificacion;

        enviarCorreo(correo, "Código de seguridad para cambio de contraseña", cuerpo);
    }

    private String generarCodigoVerificacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(6);
        for (int i = 0; i < 6; i++)
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoVerificacion() { return this.codigoVerificacion; }

    private void enviarCorreo(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    /* ==========================
       ROLES
       ========================== */
    @Override
    @Transactional
    public Usuario assignRole(Long userId, Long roleId, String roleName) {
        Usuario user = usuarioDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Rol rol;
        if (roleId != null)
            rol = rolDAO.findById(roleId).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        else if (roleName != null && !roleName.isBlank())
            rol = rolDAO.findByNombreRol(roleName).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        else throw new RuntimeException("Debe especificar roleId o roleName");

        user.setRolEntity(rol);
        user.setRol(rol.getNombreRol());
        return usuarioDAO.save(user);
    }

    @Override
    @Transactional
    public Usuario removeRole(Long userId) {
        Usuario user = usuarioDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Rol sinRol = rolDAO.findByNombreRol("SIN_ROL")
                .orElseGet(() -> rolDAO.save(new Rol("SIN_ROL")));
        user.setRolEntity(sinRol);
        user.setRol(sinRol.getNombreRol());
        return usuarioDAO.save(user);
    }

    /* ==========================
       ENVÍO DE CREDENCIALES MANUAL
       ========================== */
    @Override
    public void enviarCredencialesUsuario(Usuario usuario) {
        try {
            if (usuario == null) throw new RuntimeException("Usuario no válido.");

            String contraseniaGenerada = buildInitialPassword(
                    nullSafe(usuario.getNombre()),
                    nullSafe(usuario.getApellido()),
                    nullSafe(usuario.getCi())
            );

            String cuerpo = """
            Estimado/a %s %s,
            
            Sus credenciales de acceso al Sistema USEI son:
            
            Usuario: %s
            Contraseña: %s
            
            Por seguridad, deberá cambiar su contraseña al ingresar por primera vez.
            
            Saludos cordiales,
            Equipo USEI
            Universidad Católica Boliviana "San Pablo"
            """.formatted(usuario.getNombre(), usuario.getApellido(), usuario.getCi(), contraseniaGenerada);

            enviarCorreo(usuario.getCorreo(), "Reenvío de credenciales - Sistema USEI", cuerpo);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar credenciales: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByCi(String ci) { return usuarioDAO.existsByCi(ci); }

    /* ==========================
       Helpers
       ========================== */
    private static String nullSafe(String s) { return (s == null) ? "" : s.trim(); }

    /**
     * Regla única para generar la clave inicial:
     *   inicial del nombre (MAYÚS) + apellido completo en minúsculas (sin espacios) + CI
     * Si prefieres DOS INICIALES, cambia aquí y queda consistente en toda la clase.
     */
    private static String buildInitialPassword(String nombre, String apellido, String ci) {
        String inicialNombre = nombre.isEmpty() ? "" : nombre.substring(0,1).toUpperCase();
        String apellidoMin   = apellido.toLowerCase().replaceAll("\\s+", "");
        return inicialNombre + apellidoMin + ci;
        // Variante con 2 iniciales:
        // String inicialApellido = apellido.isEmpty() ? "" : apellido.substring(0,1).toUpperCase();
        // return inicialNombre + inicialApellido + ci;
    }
}
