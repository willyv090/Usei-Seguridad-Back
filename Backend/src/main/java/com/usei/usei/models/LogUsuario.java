package com.usei.usei.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_usuario")
public class LogUsuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "fecha_log", nullable = false)
    private LocalDateTime fechaLog;

    @Column(name = "motivo", length = 150, nullable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_id_usuario", nullable = false)
    private Usuario usuarioIdUsuario;

    public LogUsuario() {}

    public LogUsuario(LocalDateTime fechaLog, String motivo, Usuario usuario) {
        this.fechaLog = fechaLog;
        this.motivo = motivo;
        this.usuarioIdUsuario = usuario;
    }

    public Long getIdLog() { return idLog; }
    public LocalDateTime getFechaLog() { return fechaLog; }
    public String getMotivo() { return motivo; }
    public Usuario getUsuarioIdUsuario() { return usuarioIdUsuario; }

    public void setIdLog(Long idLog) { this.idLog = idLog; }
    public void setFechaLog(LocalDateTime fechaLog) { this.fechaLog = fechaLog; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setUsuarioIdUsuario(Usuario usuarioIdUsuario) { this.usuarioIdUsuario = usuarioIdUsuario; }
}
