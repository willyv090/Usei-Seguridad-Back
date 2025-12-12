package com.usei.usei.api;

import com.usei.usei.controllers.LogUsuarioQueryService;
import com.usei.usei.dto.response.LogUsuarioResponseDTO;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/log-usuario")
public class LogUsuarioAPI {

    private final LogUsuarioQueryService service;

    public LogUsuarioAPI(LogUsuarioQueryService service) {
        this.service = service;
    }

    // ✅ Tu front ya llama a:  GET {BASE_URL}/log-usuario
    @GetMapping
    public List<LogUsuarioResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    // ✅ Opcional (mejor si hay muchos registros)
    // GET {BASE_URL}/log-usuario/page?page=0&size=10&sort=fechaLog,desc
    @GetMapping("/page")
    public Page<LogUsuarioResponseDTO> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaLog,desc") String sort
    ) {
        String[] s = sort.split(",");
        String field = s[0];
        Sort.Direction dir = (s.length > 1 && "asc".equalsIgnoreCase(s[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, field));
        return service.listarPaginado(pageable);
    }
}
