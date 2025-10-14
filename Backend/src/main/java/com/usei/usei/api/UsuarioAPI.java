package com.usei.usei.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.dto.SuccessfulResponse;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.dto.request.LoginRequestUserDTO;
import com.usei.usei.models.LoginResponse;
import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;
import com.usei.usei.util.TokenGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;


import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/usuario")
public class UsuarioAPI {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenGenerator tokenGenerator;

    // Service de roles para crear/eliminar roles
    @Autowired
    private RolBL rolBL;

    // ====== CRUD ======

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Usuario usuario) {
        Usuario saved = usuarioService.save(usuario);
        // Nunca retornar contraseñas
        saved.setContrasenia(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id_usuario}")
    public ResponseEntity<?> read(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();
        oUsuario.get().setContrasenia(null);
        return ResponseEntity.ok(oUsuario.get());
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        Iterable<Usuario> all = usuarioService.findAll();
        // enmascarar contraseñas por seguridad
        all.forEach(u -> u.setContrasenia(null));
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/{id_usuario}")
    public ResponseEntity<?> delete(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();
        usuarioService.deleteById(idUsuario);
        oUsuario.get().setContrasenia(null);
        return ResponseEntity.ok(oUsuario.get());
    }

    @PutMapping("/{id_usuario}")
    public ResponseEntity<?> update(@PathVariable("id_usuario") Long idUsuario, @RequestBody Usuario usuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        oUsuario.get().setNombre(usuario.getNombre());
        oUsuario.get().setTelefono(usuario.getTelefono());
        oUsuario.get().setCorreo(usuario.getCorreo());
        oUsuario.get().setCarrera(usuario.getCarrera());
        // si sigues usando el varchar 'rol', lo mantienes
        oUsuario.get().setRol(usuario.getRol());
        oUsuario.get().setUsuario(usuario.getUsuario());
        oUsuario.get().setContrasenia(usuario.getContrasenia());

        Usuario updated = usuarioService.save(oUsuario.get());
        updated.setContrasenia(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    // ====== PASSWORD ======

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam Long idUsuario, @RequestBody HashMap<String, String> passwordData) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        oUsuario.get().setContrasenia(passwordData.get("newPassword"));
        usuarioService.save(oUsuario.get());
        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }

    // ====== LOGIN ======

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestUserDTO loginRequestUser) {
        try{
            Optional<Usuario> usuario = usuarioService.login(loginRequestUser.getCorreo(), loginRequestUser.getContrasena());
            if (usuario.isPresent()) {
                Usuario user = usuario.get();
                user.setContrasenia(null); // No enviar contraseña

                String token = tokenGenerator.generateToken(
                        String.valueOf(user.getIdUsuario()),
                        user.getRol(), // si usas entidad de rol, aquí podrías usar user.getRolEntity().getNombreRol()
                        user.getCorreo(),
                        60
                );

                Map<String, Object> data = new HashMap<>();
                data.put("id_usuario", user.getIdUsuario());
                data.put("rol", user.getRol()); // o user.getRolEntity().getNombreRol()
                data.put("correo", user.getCorreo());
                data.put("carrera", user.getCarrera());
                data.put("nombre", user.getNombre());
                data.put("usuario", user.getUsuario());

                SuccessfulResponse response = new SuccessfulResponse(
                        "200 OK",
                        "Inicio de sesión correcto",
                        token,
                        60,
                        data
                );
                return ResponseEntity.ok(response);
            } else {
                UnsuccessfulResponse response = new UnsuccessfulResponse(
                        "401 Unauthorized",
                        "Credenciales incorrectas",
                        "/usuario/login"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new LoginResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ====== EMAIL CÓDIGO ======

    @PostMapping("/enviarCodigoVerificacion/{correo}")
    public ResponseEntity<?> enviarCodigoVerificacion(@PathVariable("correo") String correo) {
        try {
            Long idDirector = usuarioService.findByMail(correo);
            if (idDirector == 0) {
                return new ResponseEntity<>("No se encontró un Director con ese correo.", HttpStatus.NOT_FOUND);
            }

            usuarioService.enviarCodigoVerificacion(correo);
            String codigoVerificacion = usuarioService.obtenerCodigoVerificacion();

            return new ResponseEntity<>(new HashMap<String, Object>() {{
                put("mensaje", "Código de verificación enviado exitosamente");
                put("codigoVerificacion", codigoVerificacion);
                put("idDirector", idDirector);
            }}, HttpStatus.OK);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error al enviar el código de verificación: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ====== ROLES ======

    // ---------- Crear Rol ----------
    public static class CrearRolRequest {
        private String nombreRol;
        public String getNombreRol() { return nombreRol; }
        public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    }

    @PostMapping("/rol")
    public ResponseEntity<?> crearRol(@RequestBody CrearRolRequest req) {
        try {
            Rol saved = rolBL.crearRol(req.getNombreRol());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new UnsuccessfulResponse("400", ex.getMessage(), "/usuario/rol")
            );
        }
    }

    // ---------- Eliminar Rol (solo si no está en uso) ----------
    @DeleteMapping("/rol/{idRol}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long idRol) {
        try {
            rolBL.eliminarRol(idRol);
            return ResponseEntity.ok("Rol eliminado");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new UnsuccessfulResponse("400", ex.getMessage(), "/usuario/rol/{idRol}")
            );
        }
    }

    // ---------- Asignar Rol existente a un Usuario ----------
    public static class AssignRoleRequest {
        private Long roleId;
        private String roleName;
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    @PostMapping("/{id_usuario}/rol")
    public ResponseEntity<?> assignRole(
            @PathVariable("id_usuario") Long idUsuario,
            @RequestBody AssignRoleRequest req) {
        try {
            Usuario updated = usuarioService.assignRole(idUsuario, req.getRoleId(), req.getRoleName());
            updated.setContrasenia(null);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UnsuccessfulResponse("400", ex.getMessage(), "/usuario/{id}/rol"));
        }
    }

    @DeleteMapping("/{id_usuario}/rol")
    public ResponseEntity<?> removeRole(@PathVariable("id_usuario") Long idUsuario) {
        try {
            Usuario updated = usuarioService.removeRole(idUsuario);
            // no exponer contraseñas
            updated.setContrasenia(null);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            // Usa el constructor real de tu UnsuccessfulResponse (sin parámetros con nombre)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new UnsuccessfulResponse(
                            "400",
                            "Error al quitar rol: " + ex.getMessage(),
                            "/usuario/" + idUsuario + "/rol"
                    ));
        }
    }

}
