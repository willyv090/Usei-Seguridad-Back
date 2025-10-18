package com.usei.usei.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.*;
import com.usei.usei.repositories.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UsuarioBL implements UsuarioService {

    @Autowired private UsuarioDAO usuarioDAO;
    @Autowired private RolDAO rolDAO;
    @Autowired private ContraseniaDAO contraseniaDAO;
    private final JavaMailSender mailSender;
    private String codigoVerificacion;

    @Autowired
    public UsuarioBL(UsuarioDAO usuarioDAO, JavaMailSender mailSender) {
        this.usuarioDAO = usuarioDAO;
        this.mailSender = mailSender;
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
        if (usuarioDAO.existsByCi(usuario.getCi()))
            throw new RuntimeException("Ya existe un usuario con el CI " + usuario.getCi());

        // Validar y crear contraseña por defecto
        if (usuario.getContraseniaEntity() == null) {
            String pass = (usuario.getNombre().substring(0, 1)
                    + usuario.getApellido().substring(0, 1)
                    + usuario.getCi()).toUpperCase();

            Contrasenia contrasenia = new Contrasenia();
            contrasenia.setContrasenia(pass);
            contrasenia.setFechaCreacion(LocalDate.now());
            contrasenia.setLongitud(pass.length());
            contrasenia.setComplejidad(1);
            contrasenia.setIntentosRestantes(3);
            contrasenia.setUltimoLog(LocalDate.now());

            contrasenia = contraseniaDAO.save(contrasenia);
            usuario.setContraseniaEntity(contrasenia);
        }

        usuario.setCambioContrasenia(true);
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
       LOGIN
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String contraseniaIngresada) {
        Usuario usuario = usuarioDAO.findByCorreo(correo);
        if (usuario == null) return Optional.empty();

        Contrasenia pass = usuario.getContraseniaEntity();
        if (pass != null && pass.getContrasenia().equals(contraseniaIngresada)) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /* ==========================
       CÓDIGO DE VERIFICACIÓN EMAIL
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Long findByMail(String correo) {
        Usuario usuario = usuarioDAO.findByCorreo(correo);
        return (usuario == null) ? 0L : usuario.getIdUsuario();
    }

    @Override
    public void enviarCodigoVerificacion(String correo) throws MessagingException {
        Usuario usuario = usuarioDAO.findByCorreo(correo);
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

    @Override
    public boolean existsByCi(String ci) { return usuarioDAO.existsByCi(ci); }
}
