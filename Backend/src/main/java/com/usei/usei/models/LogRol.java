package com.usei.usei.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log_Rol")
public class LogRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "fecha_log", nullable = false)
    private LocalDateTime fechaLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Rol_id_rol")   // FK de la tabla roles
    private Rol rol; // Rol relacionado

    @Column(name = "motivo", length = 150, nullable = false)
    private String motivo;      // Ej: CREACION_ROL, ACTUALIZACION_ROL, ELIMINACION_ROL

    @Column(name = "nivel", length = 150, nullable = false)
    private String nivel;       // INFO, ERROR, WARN

    @Column(name = "mensaje", length = 150)
    private String mensaje;     // Descripción legible de la acción

    @Column(name = "detalle", length = 255)
    private String detalle;     // Información adicional sobre la acción

    public LogRol() {}

    // Constructor de conveniencia
    public LogRol(Rol rol, String motivo, String nivel, String mensaje, String detalle) {
        this.rol = rol;
        this.motivo = motivo;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.fechaLog = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public LocalDateTime getFechaLog() { return fechaLog; }
    public void setFechaLog(LocalDateTime fechaLog) { this.fechaLog = fechaLog; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
