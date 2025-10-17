package com.usei.usei.controllers;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.RolDAO;
import com.usei.usei.repositories.UsuarioDAO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UsuarioBL implements UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;
    private final JavaMailSender mailSender;

    private String codigoVerificacion;

    @Autowired
    private RolDAO rolDAO;

    @Autowired
    public UsuarioBL(UsuarioDAO usuarioDAO, JavaMailSender mailSender) {
        this.usuarioDAO = usuarioDAO;
        this.mailSender = mailSender;
    }

    /* ==========================
       CRUD BÁSICO DE USUARIO
       ========================== */

    @Override
    @Transactional(readOnly = true)
    public Iterable<Usuario> findAll() {
        return usuarioDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioDAO.findById(id);
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        // 🔹 Validar duplicado de CI
        if (usuarioDAO.existsByCi(usuario.getCi())) {
            throw new RuntimeException("Ya existe un usuario con el CI " + usuario.getCi());
        }

        // 🔹 Si no tiene contraseña, generarla automáticamente
        if (usuario.getContrasenia() == null || usuario.getContrasenia().isBlank()) {
            String contraseniaGenerada = (usuario.getNombre().substring(0, 1)
                    + usuario.getApellido().substring(0, 1)
                    + usuario.getCi()).toUpperCase();
            usuario.setContrasenia(contraseniaGenerada);
            usuario.setCambioContrasenia(true);
        }

        return usuarioDAO.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioDAO.deleteById(id);
    }

    @Override
    @Transactional
    public Usuario update(Usuario usuario, Long id) {
        Usuario usuarioToUpdate = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        usuarioToUpdate.setNombre(usuario.getNombre());
        usuarioToUpdate.setApellido(usuario.getApellido());
        usuarioToUpdate.setTelefono(usuario.getTelefono());
        usuarioToUpdate.setCorreo(usuario.getCorreo());
        usuarioToUpdate.setCarrera(usuario.getCarrera());
        usuarioToUpdate.setCi(usuario.getCi());
        usuarioToUpdate.setContrasenia(usuario.getContrasenia());
        usuarioToUpdate.setCambioContrasenia(usuario.getCambioContrasenia());

        // Mantienes también el campo texto 'rol' (varchar)
        try { usuarioToUpdate.setRol(usuario.getRol()); } catch (Exception ignored) {}

        // Si te envían la relación ya resuelta (no obligatorio)
        if (usuario.getRolEntity() != null) {
            usuarioToUpdate.setRolEntity(usuario.getRolEntity());
            try { usuarioToUpdate.setRol(usuario.getRolEntity().getNombreRol()); } catch (Exception ignored) {}
        }

        return usuarioDAO.save(usuarioToUpdate);
    }

    /* ==========================
       AUTENTICACIÓN
       ========================== */

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String contrasenia) {
        return usuarioDAO.findByCorreoAndContrasenia(correo, contrasenia);
    }

    /* ==========================
       CÓDIGO DE VERIFICACIÓN (EMAIL)
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
        if (usuario == null) throw new MessagingException("No existe usuario con correo: " + correo);

        String asunto = "Código de seguridad para cambio de contraseña";
        String mensaje = "Estimado usuario, ha solicitado un cambio de contraseña. "
                + "Por favor ingrese el siguiente código de seguridad: ";

        this.codigoVerificacion = generarCodigoVerificacion();
        String cuerpoCorreo = mensaje + this.codigoVerificacion;
        enviarCorreo(correo, asunto, cuerpoCorreo);
        System.out.println("Correo enviado a: " + usuario.getNombre());
    }

    private String generarCodigoVerificacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(6);
        for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoVerificacion() {
        return this.codigoVerificacion;
    }

    private void enviarCorreo(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    /* ==========================
       ROLES: asignar rol existente
       ========================== */
    @Override
    @Transactional
    public Usuario assignRole(Long userId, Long roleId, String roleName) {
        Usuario user = usuarioDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Rol rol;
        if (roleId != null) {
            rol = rolDAO.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleId));
        } else if (roleName != null && !roleName.isBlank()) {
            rol = rolDAO.findByNombreRol(roleName.trim())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
        } else {
            throw new RuntimeException("Debes enviar roleId o roleName");
        }

        user.setRolEntity(rol);
        try { user.setRol(rol.getNombreRol()); } catch (Exception ignored) {}

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
        try { user.setRol(sinRol.getNombreRol()); } catch (Exception ignored) {}

        return usuarioDAO.save(user);
    }

    /* ==========================
       NUEVO: verificar duplicado de CI
       ========================== */
    @Override
    public boolean existsByCi(String ci) {
        return usuarioDAO.existsByCi(ci);
    }

}
