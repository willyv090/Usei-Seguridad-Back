package com.usei.usei.models;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    // ✅ accesos ahora es texto simple (VARCHAR en la BD)
    @Column(name = "accesos", length = 255)
    private String accesos;

    public Rol() {}

    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    // --- Getters y Setters ---
    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getAccesos() { return accesos; }
    public void setAccesos(String accesos) { this.accesos = accesos; }
}
