package com.usei.usei.controllers;

import com.usei.usei.models.Estudiante;
import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EstudianteService {

    public Iterable<Estudiante> findAll();

    public Optional<Estudiante> findById(Long id);

    public Estudiante save(Estudiante newEstudiante);

    public void deleteById(Long id);

    public Estudiante update (Estudiante newEstudiante, Long id);

    public Optional<Estudiante> login(int ci, String contrasena);
    
    // New unified login method that returns LoginStatus for consistency with Usuario login
    public LoginStatus loginWithStatus(int ci, String contrasena);
    
    Optional<Estudiante> findByCi(int ci);

    // Método para enviar el correo
    public void enviarCorreosEstudiantes() throws MessagingException;

    public void enviarCodigoVerificacion(String correo) throws MessagingException;

    public String obtenerCodigoVerificacion();

    public Long findByCorreoInst(String correo) throws MessagingException;
    
    public List<Estudiante> saveAll(List<Estudiante> estudiantes);

    Page<Estudiante> findByNombreContainingOrCiContaining(String nombre, String ci, Pageable pageable);

    // Nuevos métodos separados para buscar por nombre y CI
    Page<Estudiante> findByNombre(String nombre, Pageable pageable); // Búsqueda por nombre

    Page<Estudiante> findByCi(Integer ci, Pageable pageable); // Búsqueda por CI

    Page<Estudiante> findAll(Pageable pageable); // Obtener todos los estudiantes con paginación

    Optional<Estudiante> existingStudent(int ci);

    //List<Estudiante> findEstudiantesNoCompletaronEncuesta();

    List<Integer> findDistinctAnios();
    List<Integer> findDistinctSemestres();
    List<Estudiante> findNoCompletaronEncuestaByAnioAndSemestre(Integer anio, Integer semestre);
    List<Object[]> getEstudiantesCompletaronEncuestaByGeneroAndAnio(Integer anio);

    public List<Integer> findUniqueYears();
    List<Estudiante> findByCarrera(String carrera);


}
