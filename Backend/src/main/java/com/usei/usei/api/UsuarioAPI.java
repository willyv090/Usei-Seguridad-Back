package com.usei.usei.api;

import java.util.*;

import com.usei.usei.dto.response.UsuarioResponseDTO;
import com.usei.usei.models.Contrasenia;
import com.usei.usei.repositories.ContraseniaDAO;
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

    @Autowired
    private RolBL rolBL;

    @Autowired
    private ContraseniaDAO contraseniaDAO;

    // ===========================
    // CREAR USUARIO
    // ===========================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("nombre") || !body.containsKey("apellido")
                    || !body.containsKey("ci") || !body.containsKey("idRol")) {
                return ResponseEntity.badRequest()
                        .body("Faltan campos obligatorios: nombre, apellido, ci, idRol");
            }

            String nombre = body.get("nombre").toString().trim();
            String apellido = body.get("apellido").toString().trim();
            String ci = body.get("ci").toString().trim();
            int telefono = body.get("telefono") != null ? Integer.parseInt(body.get("telefono").toString()) : 0;
            String correo = body.get("correo") != null ? body.get("correo").toString().trim() : "";
            String carrera = body.get("carrera") != null ? body.get("carrera").toString().trim() : "";

            Long idRol = Long.parseLong(body.get("idRol").toString());
            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El rol especificado no existe.");

            // 1️⃣ Generar contraseña por defecto
            String contraseniaGenerada = (nombre.substring(0, 1) + apellido.substring(0, 1) + ci).toUpperCase();

            // 2️⃣ Crear entidad Contrasenia
            Contrasenia nuevaPass = new Contrasenia(contraseniaGenerada, contraseniaGenerada.length(), 1);
            Contrasenia savedPass = contraseniaDAO.save(nuevaPass);

            // 3️⃣ Crear usuario con FK a Contrasenia
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(apellido);
            nuevoUsuario.setCi(ci);
            nuevoUsuario.setTelefono(telefono);
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setCarrera(carrera);
            nuevoUsuario.setRol(rol.getNombreRol());
            nuevoUsuario.setRolEntity(rol);
            nuevoUsuario.setContraseniaEntity(savedPass);
            nuevoUsuario.setCambioContrasenia(true);

            Usuario saved = usuarioService.save(nuevoUsuario);
            saved.setContraseniaEntity(null); // ocultar contraseña en respuesta
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage());
        }
    }

    // ===========================
    // OBTENER USUARIO POR ID
    // ===========================
    @GetMapping("/{id_usuario}")
    public ResponseEntity<?> read(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        Usuario usuario = oUsuario.get();
        usuario.setContraseniaEntity(null);
        return ResponseEntity.ok(usuario);
    }

    // ===========================
    // LISTAR TODOS LOS USUARIOS
    // ===========================
    @GetMapping
    public ResponseEntity<?> readAll() {
        try {
            List<UsuarioResponseDTO> usuarios = new ArrayList<>();
            usuarioService.findAll().forEach(u -> {
                String rol = (u.getRol() != null && !u.getRol().isBlank())
                        ? u.getRol()
                        : (u.getRolEntity() != null ? u.getRolEntity().getNombreRol() : "SIN_ROL");

                usuarios.add(new UsuarioResponseDTO(
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getCorreo(),
                        u.getCi(),
                        u.getTelefono(),
                        u.getCarrera(),
                        rol
                ));
            });

            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar usuarios: " + e.getMessage());
        }
    }

    // ===========================
    // ELIMINAR USUARIO
    // ===========================
    @DeleteMapping("/{id_usuario}")
    public ResponseEntity<?> delete(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        usuarioService.deleteById(idUsuario);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }

    // ===========================
    // ACTUALIZAR USUARIO (TOTAL)
    // ===========================
    @PutMapping("/{id_usuario}")
    public ResponseEntity<?> update(@PathVariable("id_usuario") Long idUsuario, @RequestBody Usuario usuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        Usuario u = oUsuario.get();
        u.setNombre(usuario.getNombre());
        u.setApellido(usuario.getApellido());
        u.setTelefono(usuario.getTelefono());
        u.setCorreo(usuario.getCorreo());
        u.setCarrera(usuario.getCarrera());
        u.setRol(usuario.getRol());
        u.setCi(usuario.getCi());

        Usuario updated = usuarioService.save(u);
        updated.setContraseniaEntity(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    // ===========================
    // ACTUALIZAR USUARIO (PARCIAL - PATCH)
    // ===========================
    @PatchMapping("/{id_usuario}")
        public ResponseEntity<?> editarUsuario(
                @PathVariable("id_usuario") Long idUsuario,
                @RequestBody Map<String, Object> body) {
            try {
                Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
                if (oUsuario.isEmpty())
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");

                Usuario u = oUsuario.get();

                // 🔹 Actualiza solo los campos enviados
                if (body.containsKey("nombre")) u.setNombre((String) body.get("nombre"));
                if (body.containsKey("apellido")) u.setApellido((String) body.get("apellido"));
                if (body.containsKey("ci")) u.setCi((String) body.get("ci"));
                if (body.containsKey("correo")) u.setCorreo((String) body.get("correo"));

                if (body.containsKey("telefono")) {
                    String telStr = String.valueOf(body.get("telefono"));
                    if (telStr != null && !telStr.isBlank() && !telStr.equals("null")) {
                        try {
                            u.setTelefono(Integer.parseInt(telStr));
                        } catch (NumberFormatException ex) {
                            return ResponseEntity.badRequest()
                                    .body("El campo 'telefono' debe ser numérico.");
                        }
                    }
                }

                if (body.containsKey("carrera")) u.setCarrera((String) body.get("carrera"));

                // 🔹 Actualizar rol si llega idRol o rol
                if (body.containsKey("idRol") || body.containsKey("rol")) {
                    Rol nuevoRol = null;

                    if (body.containsKey("idRol")) {
                        String idRolStr = String.valueOf(body.get("idRol"));
                        if (idRolStr != null && !idRolStr.isBlank() && !idRolStr.equals("null")) {
                            try {
                                Long idRol = Long.parseLong(idRolStr);
                                nuevoRol = rolBL.obtenerRolPorId(idRol);
                            } catch (NumberFormatException ex) {
                                return ResponseEntity.badRequest()
                                        .body("El campo 'idRol' debe ser un número válido.");
                            }
                        }
                    } else if (body.containsKey("rol")) {
                        String nombreRol = String.valueOf(body.get("rol"));
                        if (nombreRol != null && !nombreRol.isBlank()) {
                            nuevoRol = rolBL.obtenerRolPorNombre(nombreRol);
                        }
                    }

                    if (nuevoRol == null) {
                        return ResponseEntity.badRequest()
                                .body("El rol especificado no existe o no es válido.");
                    }

                    u.setRolEntity(nuevoRol);
                    u.setRol(nuevoRol.getNombreRol());
                }

                Usuario actualizado = usuarioService.save(u);
                actualizado.setContraseniaEntity(null);
                return ResponseEntity.ok(actualizado);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al actualizar usuario: " + e.getMessage());
            }
        }


    // ===========================
    // CAMBIAR CONTRASEÑA
    // ===========================
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam Long idUsuario, @RequestBody HashMap<String, String> passwordData) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        Usuario usuario = oUsuario.get();
        String nuevaPassStr = passwordData.get("newPassword");

        if (nuevaPassStr == null || nuevaPassStr.isBlank())
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía.");

        Contrasenia nuevaPass = new Contrasenia(nuevaPassStr, nuevaPassStr.length(), 1);
        Contrasenia savedPass = contraseniaDAO.save(nuevaPass);

        usuario.setContraseniaEntity(savedPass);
        usuario.setCambioContrasenia(false);
        usuarioService.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }

    // ===========================
    // LOGIN
    // ===========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestUserDTO loginRequestUser) {
        try {
            Optional<Usuario> usuario = usuarioService.login(loginRequestUser.getCorreo(), loginRequestUser.getContrasena());
            if (usuario.isPresent()) {
                Usuario user = usuario.get();
                user.setContraseniaEntity(null);

                String token = tokenGenerator.generateToken(
                        String.valueOf(user.getIdUsuario()),
                        user.getRol(),
                        user.getCorreo(),
                        60
                );

                Map<String, Object> data = new HashMap<>();
                data.put("id_usuario", user.getIdUsuario());
                data.put("rol", user.getRol());
                data.put("correo", user.getCorreo());
                data.put("carrera", user.getCarrera());
                data.put("nombre", user.getNombre());
                data.put("ci", user.getCi());
                data.put("cambio_contrasenia", user.getCambioContrasenia());

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

    // ===========================
    // EMAIL: ENVIAR CÓDIGO
    // ===========================
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

    // ===========================
    // ROLES
    // ===========================
    @GetMapping("/rol")
    public ResponseEntity<?> listarRoles() {
        try {
            Iterable<Rol> roles = rolBL.listarRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UnsuccessfulResponse("500", "Error al listar roles: " + ex.getMessage(), "/usuario/rol")
            );
        }
    }

    @PutMapping("/rol/{idRol}/estado")
    public ResponseEntity<?> actualizarEstadoRol(@PathVariable Long idRol, @RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("activo")) {
                return ResponseEntity.badRequest().body("Campo 'activo' es obligatorio.");
            }

            boolean nuevoEstado = Boolean.parseBoolean(String.valueOf(body.get("activo")));
            Rol rolActualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);

            return ResponseEntity.ok(rolActualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }
}
