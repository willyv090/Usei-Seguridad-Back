package com.usei.usei.controllers;

import com.usei.usei.dto.IndicadorRiesgoDTO;
import com.usei.usei.models.IndicadorRiesgo;
import com.usei.usei.models.RiesgoEvento;
import com.usei.usei.repositories.IndicadorRiesgoDAO;
import com.usei.usei.repositories.RiesgoEventoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IndicadorRiesgoBL {
    
    private static final Logger logger = LoggerFactory.getLogger(IndicadorRiesgoBL.class);
    
    @Autowired
    private IndicadorRiesgoDAO indicadorRiesgoDAO;
    
    @Autowired
    private RiesgoEventoDAO riesgoEventoDAO;
    
    // Actualizar KRI automáticamente basado en estadísticas de riesgos
    @Transactional
    public void actualizarKRIAutomaticamente() {
        logger.info("Actualizando KRI automáticamente basado en estadísticas de riesgos");
        
        try {
            // Calcular estadísticas directamente desde el DAO
            List<RiesgoEvento> todosLosRiesgos = riesgoEventoDAO.findAll();
            
            int totalRiesgos = todosLosRiesgos.size();
            int riesgosCriticos = (int) todosLosRiesgos.stream()
                .filter(r -> "Crítico".equalsIgnoreCase(r.getNivelRiesgo()))
                .count();
            int riesgosAltos = (int) todosLosRiesgos.stream()
                .filter(r -> "Alto".equalsIgnoreCase(r.getNivelRiesgo()))
                .count();
            
            logger.info("Estadísticas calculadas - Total: {}, Críticos: {}, Altos: {}", 
                totalRiesgos, riesgosCriticos, riesgosAltos);
            
            // Buscar y actualizar KRI específicos por nombre
            List<IndicadorRiesgo> indicadores = indicadorRiesgoDAO.findByActivoTrue();
            
            for (IndicadorRiesgo indicador : indicadores) {
                BigDecimal nuevoValor = null;
                
                // Actualizar según el tipo de indicador
                if (indicador.getNombre().toLowerCase().contains("intentos de acceso")) {
                    // KRI: Intentos de Acceso No Autorizados
                    // Usar el TOTAL de riesgos registrados (todos los riesgos)
                    nuevoValor = BigDecimal.valueOf(totalRiesgos);
                    
                } else if (indicador.getNombre().toLowerCase().contains("contraseña")) {
                    // KRI: Contraseñas No Conformes
                    // También usar el total de riesgos para mostrar todos
                    nuevoValor = BigDecimal.valueOf(totalRiesgos);
                }
                
                // Actualizar el valor si fue calculado
                if (nuevoValor != null && !nuevoValor.equals(indicador.getValorActual())) {
                    indicador.setValorActual(nuevoValor);
                    indicador.setUltimaActualizacion(LocalDateTime.now());
                    indicadorRiesgoDAO.save(indicador);
                    logger.info("KRI actualizado: {} = {}", indicador.getNombre(), nuevoValor);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al actualizar KRI automáticamente", e);
        }
    }
    
    // Obtener todos los indicadores activos
    public List<IndicadorRiesgo> obtenerIndicadoresActivos() {
        return indicadorRiesgoDAO.findByActivoTrue();
    }
    
    // Obtener indicador por ID
    public Optional<IndicadorRiesgo> obtenerIndicadorPorId(Long idIndicador) {
        return indicadorRiesgoDAO.findById(idIndicador);
    }
    
    // Obtener indicadores en alerta (crítico o advertencia)
    public List<IndicadorRiesgo> obtenerIndicadoresEnAlerta() {
        return indicadorRiesgoDAO.findIndicadoresEnAlerta();
    }
    
    // Actualizar valor de indicador
    @Transactional
    public IndicadorRiesgo actualizarValorIndicador(Long idIndicador, BigDecimal nuevoValor) {
        logger.info("Actualizando valor de indicador ID: {} a {}", idIndicador, nuevoValor);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isEmpty()) {
            throw new RuntimeException("Indicador no encontrado con ID: " + idIndicador);
        }
        
        IndicadorRiesgo indicador = optionalIndicador.get();
        indicador.setValorActual(nuevoValor);
        indicador.setUltimaActualizacion(LocalDateTime.now());
        
        // El método evaluarEstado() se llama automáticamente en @PreUpdate
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Crear nuevo indicador
    @Transactional
    public IndicadorRiesgo crearIndicador(IndicadorRiesgoDTO dto) {
        logger.info("Creando nuevo indicador: {}", dto.getNombre());
        
        IndicadorRiesgo indicador = new IndicadorRiesgo();
        indicador.setNombre(dto.getNombre());
        indicador.setDescripcion(dto.getDescripcion());
        indicador.setTipoIndicador(dto.getTipoIndicador());
        indicador.setUmbralCritico(dto.getUmbralCritico());
        indicador.setUmbralAdvertencia(dto.getUmbralAdvertencia());
        indicador.setValorActual(dto.getValorActual() != null ? dto.getValorActual() : BigDecimal.ZERO);
        indicador.setUnidadMedida(dto.getUnidadMedida());
        indicador.setFrecuenciaMedicion(dto.getFrecuenciaMedicion());
        indicador.setUsuarioCreacion(dto.getUsuarioCreacion());
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Actualizar indicador
    @Transactional
    public IndicadorRiesgo actualizarIndicador(Long idIndicador, IndicadorRiesgoDTO dto) {
        logger.info("Actualizando indicador ID: {}", idIndicador);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isEmpty()) {
            throw new RuntimeException("Indicador no encontrado con ID: " + idIndicador);
        }
        
        IndicadorRiesgo indicador = optionalIndicador.get();
        indicador.setNombre(dto.getNombre());
        indicador.setDescripcion(dto.getDescripcion());
        indicador.setTipoIndicador(dto.getTipoIndicador());
        indicador.setUmbralCritico(dto.getUmbralCritico());
        indicador.setUmbralAdvertencia(dto.getUmbralAdvertencia());
        indicador.setValorActual(dto.getValorActual());
        indicador.setUnidadMedida(dto.getUnidadMedida());
        indicador.setFrecuenciaMedicion(dto.getFrecuenciaMedicion());
        indicador.setActivo(dto.getActivo());
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Desactivar indicador
    @Transactional
    public void desactivarIndicador(Long idIndicador) {
        logger.info("Desactivando indicador ID: {}", idIndicador);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isPresent()) {
            IndicadorRiesgo indicador = optionalIndicador.get();
            indicador.setActivo(false);
            indicadorRiesgoDAO.save(indicador);
        }
    }
    
    // Convertir Entity a DTO
    public IndicadorRiesgoDTO convertirADTO(IndicadorRiesgo indicador) {
        IndicadorRiesgoDTO dto = new IndicadorRiesgoDTO();
        dto.setIdIndicador(indicador.getIdIndicador());
        dto.setNombre(indicador.getNombre());
        dto.setDescripcion(indicador.getDescripcion());
        dto.setTipoIndicador(indicador.getTipoIndicador());
        dto.setUmbralCritico(indicador.getUmbralCritico());
        dto.setUmbralAdvertencia(indicador.getUmbralAdvertencia());
        dto.setValorActual(indicador.getValorActual());
        dto.setUnidadMedida(indicador.getUnidadMedida());
        dto.setFrecuenciaMedicion(indicador.getFrecuenciaMedicion());
        dto.setEstadoActual(indicador.getEstadoActual());
        dto.setUltimaActualizacion(indicador.getUltimaActualizacion());
        dto.setFechaCreacion(indicador.getFechaCreacion());
        dto.setActivo(indicador.getActivo());
        dto.setUsuarioCreacion(indicador.getUsuarioCreacion());
        return dto;
    }
}
