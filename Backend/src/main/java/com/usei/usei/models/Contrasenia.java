package com.usei.usei.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrasenia")
public class Contrasenia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pass")
    private Long idPass;

    @Column(name = "contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "longitud", nullable = false)
    private int longitud;

    @Column(name = "complejidad")
    private int complejidad = 1;

    @Column(name = "intentos_restantes")
    private int intentosRestantes = 3;

    @Column(name = "ultimo_log")
    private LocalDateTime ultimoLog;

    public Contrasenia() {}

    public Contrasenia(String contrasenia, int longitud, int complejidad) {
        this.contrasenia = contrasenia;
        this.longitud = longitud;
        this.complejidad = complejidad;
        this.intentosRestantes = 3;
    }

    // Getters y setters
    public Long getIdPass() { return idPass; }
    public void setIdPass(Long idPass) { this.idPass = idPass; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getLongitud() { return longitud; }
    public void setLongitud(int longitud) { this.longitud = longitud; }

    public int getComplejidad() { return complejidad; }
    public void setComplejidad(int complejidad) { this.complejidad = complejidad; }

    public int getIntentosRestantes() { return intentosRestantes; }
    public void setIntentosRestantes(int intentosRestantes) { this.intentosRestantes = intentosRestantes; }

    public LocalDateTime getUltimoLog() { return ultimoLog; }
    public void setUltimoLog(LocalDateTime ultimoLog) { this.ultimoLog = ultimoLog; }
}
