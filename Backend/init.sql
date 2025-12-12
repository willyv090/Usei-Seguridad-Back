-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2025-10-18 02:41:59.666

-- tables
-- Table: Certificado
CREATE TABLE Certificado (
    id_certificado serial  NOT NULL,
    formato varchar(50)  NOT NULL,
    version int  NOT NULL,
    estado varchar(20)  NOT NULL,
    fecha_modificacion date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Certificado_pk PRIMARY KEY (id_certificado)
);

-- Table: Contrasenia
CREATE TABLE Contrasenia (
    id_pass serial  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    fecha_creacion date  NOT NULL,
    longitud int  NOT NULL,
    complejidad int  NOT NULL,
    intentos_restantes int  NOT NULL,
    ultimo_log date  NOT NULL,
    CONSTRAINT Contrasenia_pk PRIMARY KEY (id_pass)
);

-- Table: Encuesta
CREATE TABLE Encuesta (
    id_encuesta serial  NOT NULL,
    titulo varchar(40)  NOT NULL,
    descripcion text  NOT NULL,
    fecha_modificado date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    Plazo_id_plazo int  NOT NULL,
    CONSTRAINT Encuesta_pk PRIMARY KEY (id_encuesta)
);

-- Table: Encuesta_Gestion
CREATE TABLE Encuesta_Gestion (
    id_encuesta_gestion serial  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Encuesta_id_encuesta int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    CONSTRAINT Encuesta_Gestion_pk PRIMARY KEY (id_encuesta_gestion)
);

-- Table: Estado_Certificado
CREATE TABLE Estado_Certificado (
    id_est_certificado serial  NOT NULL,
    archivo varchar(50)  NOT NULL,
    estado varchar(15)  NOT NULL,
    fecha_estado date  NOT NULL,
    Certificado_id_certificado int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    CONSTRAINT Estado_Certificado_pk PRIMARY KEY (id_est_certificado)
);

-- Table: Estado_Encuesta
CREATE TABLE Estado_Encuesta (
    id_est_encuesta serial  NOT NULL,
    estado varchar(30)  NOT NULL,
    fecha_estado date  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    Encuesta_id_encuesta int  NOT NULL,
    CONSTRAINT Estado_Encuesta_pk PRIMARY KEY (id_est_encuesta)
);

-- Table: Estudiante
CREATE TABLE Estudiante (
    id_estudiante serial  NOT NULL,
    ci int  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    correo_institucional varchar(50)  NOT NULL,
    correo_personal varchar(50)  NULL,
    carrera varchar(50)  NOT NULL,
    asignatura varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    estado_invitacion varchar(50)  NOT NULL,
    contrasena varchar(100)  NOT NULL,
    intentos_restantes int  NOT NULL DEFAULT 3,
    CONSTRAINT Estudiante_pk PRIMARY KEY (id_estudiante)
);

-- Table: H_Certificado
CREATE TABLE H_Certificado (
    id_certificado serial  NOT NULL,
    formato varchar(50)  NOT NULL,
    version int  NOT NULL,
    fecha_modificacion date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Certificado_pk PRIMARY KEY (id_certificado)
);

-- Table: H_Contrasenia
CREATE TABLE H_Contrasenia (
    id_pass serial  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    fecha_creacion date  NOT NULL,
    longitud int  NOT NULL,
    complejidad int  NOT NULL,
    intentos_restantes int  NOT NULL,
    ultimo_log date  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Contrasenia_pk PRIMARY KEY (id_pass)
);

-- Table: H_Estudiante
CREATE TABLE H_Estudiante (
    id_estudiante serial  NOT NULL,
    ci int  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    correo_insitucional varchar(50)  NOT NULL,
    correo_personal varchar(50)  NULL,
    carrera varchar(40)  NOT NULL,
    asignatura varchar(30)  NOT NULL,
    telefono int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Estudiante_pk PRIMARY KEY (id_estudiante)
);

-- Table: H_Noticias
CREATE TABLE H_Noticias (
    id_noticia serial  NOT NULL,
    titulo varchar(50)  NOT NULL,
    descripcion text  NOT NULL,
    img varchar(50)  NOT NULL,
    fecha_modificado date  NOT NULL,
    estado varchar(30)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Noticias_pk PRIMARY KEY (id_noticia)
);

-- Table: H_Reporte
CREATE TABLE H_Reporte (
    id_reporte serial  NOT NULL,
    titulo varchar(30)  NOT NULL,
    descripcion text  NOT NULL,
    formato varchar(50)  NOT NULL,
    fecha date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Reporte_pk PRIMARY KEY (id_reporte)
);

-- Table: H_Respuesta
CREATE TABLE H_Respuesta (
    id_respuesta serial  NOT NULL,
    respuesta text  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Respuesta_pk PRIMARY KEY (id_respuesta)
);

-- Table: H_Usuario
CREATE TABLE H_Usuario (
    id_usuario serial  NOT NULL,
    nombre varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    correo varchar(40)  NOT NULL,
    rol varchar(20)  NOT NULL,
    usuario varchar(30)  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Usuario_pk PRIMARY KEY (id_usuario)
);
-- Table: Log_Usuario
CREATE TABLE Log_Usuario (
    id_log serial  NOT NULL,
    fecha_log timestamp  NOT NULL,
    tipo_log varchar(150)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    modulo varchar(150)  NOT NULL,
    motivo varchar(150)  NOT NULL,
    nivel varchar(150)  NOT NULL,
    mensaje varchar(150)  NOT NULL,
    detalle varchar(150)  NOT NULL,
    CONSTRAINT Log_Usuario_pk PRIMARY KEY (id_log)
);
-- Table: Noticias
CREATE TABLE Noticias (
    id_noticia serial  NOT NULL,
    titulo varchar(50)  NOT NULL,
    descripcion text  NOT NULL,
    img varchar(50)  NOT NULL,
    fecha_modificado date  NOT NULL,
    estado varchar(30)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Noticias_pk PRIMARY KEY (id_noticia)
);

-- Table: Notificacion
CREATE TABLE Notificacion (
    id_notificacion serial  NOT NULL,
    titulo varchar(80)  NOT NULL,
    contenido text  NOT NULL,
    fecha timestamp  NOT NULL,
    estado_notificacion boolean  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    Tipo_Notificacion_id_notificacion int  NOT NULL,
    CONSTRAINT Notificacion_pk PRIMARY KEY (id_notificacion)
);

-- Table: Opciones_Pregunta
CREATE TABLE Opciones_Pregunta (
    id_opciones serial  NOT NULL,
    opcion varchar(30)  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    CONSTRAINT Opciones_Pregunta_pk PRIMARY KEY (id_opciones)
);

-- Table: Parametros_Aviso
CREATE TABLE Parametros_Aviso (
    id_parametro serial  NOT NULL,
    porcentaje int  NOT NULL,
    fecha_cambio date  NOT NULL,
    fecha_notificacion date  NOT NULL,
    mensaje_predeterminado text  NOT NULL,
    CONSTRAINT Parametros_Aviso_pk PRIMARY KEY (id_parametro)
);

-- Table: Plazo
CREATE TABLE Plazo (
    id_plazo serial  NOT NULL,
    Fecha_finalizacion date  NOT NULL,
    fecha_modificacion date  NOT NULL,
    estado varchar(50)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT id_plazo PRIMARY KEY (id_plazo)
);

-- Table: Pregunta
CREATE TABLE Pregunta (
    id_pregunta serial  NOT NULL,
    num_pregunta int  NOT NULL,
    pregunta text  NOT NULL,
    tipo_pregunta varchar(15)  NOT NULL,
    estado varchar(15)  NOT NULL,
    CONSTRAINT Pregunta_pk PRIMARY KEY (id_pregunta)
);

-- Table: Reporte
CREATE TABLE Reporte (
    id_reporte serial  NOT NULL,
    titulo varchar(30)  NOT NULL,
    descripcion text  NOT NULL,
    formato varchar(50)  NOT NULL,
    fecha date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Reporte_pk PRIMARY KEY (id_reporte)
);

-- Table: Respuesta
CREATE TABLE Respuesta (
    id_respuesta serial  NOT NULL,
    respuesta text  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    CONSTRAINT Respuesta_pk PRIMARY KEY (id_respuesta)
);

-- Table: Roles
CREATE TABLE Roles (
    id_rol serial  NOT NULL,
    nombre_rol varchar(50)  NOT NULL,
    activo boolean  NOT NULL,
    accesos varchar(255)  NOT NULL,
    CONSTRAINT Roles_pk PRIMARY KEY (id_rol)
);

-- Table: Soporte
CREATE TABLE Soporte (
    id_soporte serial  NOT NULL,
    mensaje text  NOT NULL,
    fecha timestamp  NOT NULL,
    Tipo_Problema_id_problema int  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Soporte_pk PRIMARY KEY (id_soporte)
);

-- Table: Tipo_Notificacion
CREATE TABLE Tipo_Notificacion (
    id_notificacion serial  NOT NULL,
    tipo varchar(80)  NOT NULL,
    CONSTRAINT Tipo_Notificacion_pk PRIMARY KEY (id_notificacion)
);

-- Table: Tipo_Problema
CREATE TABLE Tipo_Problema (
    id_problema serial  NOT NULL,
    problema varchar(80)  NOT NULL,
    CONSTRAINT Tipo_Problema_pk PRIMARY KEY (id_problema)
);

-- Table: Usuario
CREATE TABLE Usuario (
    id_usuario serial  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    correo varchar(40)  NOT NULL,
    carrera varchar(40)  NOT NULL,
    rol varchar(20)  NOT NULL,
    ci varchar(20)  NOT NULL,
    intentos_restantes int  NOT NULL DEFAULT 3,
    Roles_id_rol int  NOT NULL,
    Contrasenia_id_pass int  NOT NULL,
    CONSTRAINT Usuario_pk PRIMARY KEY (id_usuario)
);

-- Table: Log_Usuario
CREATE TABLE Log_Usuario (
    id_log serial NOT NULL,
    fecha_log timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo varchar(150) NOT NULL,
    Usuario_id_usuario int NOT NULL,
    CONSTRAINT Log_Usuario_pk PRIMARY KEY (id_log)
);

--Esto acelera las búsquedas por usuario
CREATE INDEX idx_log_usuario_user
ON Log_Usuario (Usuario_id_usuario);


-- Table: ConfiguracionSeguridad - Configurable security policies for Security role users
CREATE TABLE configuracion_seguridad (
    id_config serial  NOT NULL,
    min_longitud_contrasenia int  NOT NULL DEFAULT 12,
    max_intentos_login int  NOT NULL DEFAULT 3,
    dias_expiracion_contrasenia int  NOT NULL DEFAULT 60,
    meses_no_reutilizar int  NOT NULL DEFAULT 12,
    requerir_mayusculas boolean  NOT NULL DEFAULT true,
    requerir_minusculas boolean  NOT NULL DEFAULT true,
    requerir_numeros boolean  NOT NULL DEFAULT true,
    requerir_simbolos boolean  NOT NULL DEFAULT true,
    fecha_modificacion timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_modificacion int  NOT NULL DEFAULT 1,
    activa boolean  NOT NULL DEFAULT true,
    CONSTRAINT configuracion_seguridad_pk PRIMARY KEY (id_config)
);

-- foreign keys
-- Reference: Certificado_Usuario (table: Certificado)
ALTER TABLE Certificado ADD CONSTRAINT Certificado_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Gestion_Encuesta (table: Encuesta_Gestion)
ALTER TABLE Encuesta_Gestion ADD CONSTRAINT Encuesta_Gestion_Encuesta
    FOREIGN KEY (Encuesta_id_encuesta)
    REFERENCES Encuesta (id_encuesta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Gestion_Pregunta (table: Encuesta_Gestion)
ALTER TABLE Encuesta_Gestion ADD CONSTRAINT Encuesta_Gestion_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Plazo (table: Encuesta)
ALTER TABLE Encuesta ADD CONSTRAINT Encuesta_Plazo
    FOREIGN KEY (Plazo_id_plazo)
    REFERENCES Plazo (id_plazo)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Usuario (table: Encuesta)
ALTER TABLE Encuesta ADD CONSTRAINT Encuesta_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Certificado_Certificado (table: Estado_Certificado)
ALTER TABLE Estado_Certificado ADD CONSTRAINT Estado_Certificado_Certificado
    FOREIGN KEY (Certificado_id_certificado)
    REFERENCES Certificado (id_certificado)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;
-- Reference: Log_Usuario_Usuario (table: Log_Usuario)
ALTER TABLE Log_Usuario ADD CONSTRAINT Log_Usuario_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;
-- Reference: Estado_Certificado_Estudiante (table: Estado_Certificado)
ALTER TABLE Estado_Certificado ADD CONSTRAINT Estado_Certificado_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Encuesta_Encuesta (table: Estado_Encuesta)
ALTER TABLE Estado_Encuesta ADD CONSTRAINT Estado_Encuesta_Encuesta
    FOREIGN KEY (Encuesta_id_encuesta)
    REFERENCES Encuesta (id_encuesta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Encuesta_Estudiante (table: Estado_Encuesta)
ALTER TABLE Estado_Encuesta ADD CONSTRAINT Estado_Encuesta_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Noticias_Usuario (table: Noticias)
ALTER TABLE Noticias ADD CONSTRAINT Noticias_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Notificacion_Tipo_Notificacion (table: Notificacion)
ALTER TABLE Notificacion ADD CONSTRAINT Notificacion_Tipo_Notificacion
    FOREIGN KEY (Tipo_Notificacion_id_notificacion)
    REFERENCES Tipo_Notificacion (id_notificacion)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Notificaciones_Estudiante (table: Notificacion)
ALTER TABLE Notificacion ADD CONSTRAINT Notificaciones_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Opciones_Pregunta_Pregunta (table: Opciones_Pregunta)
ALTER TABLE Opciones_Pregunta ADD CONSTRAINT Opciones_Pregunta_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: PLazo_Usuario (table: Plazo)
ALTER TABLE Plazo ADD CONSTRAINT PLazo_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Reporte_Usuario (table: Reporte)
ALTER TABLE Reporte ADD CONSTRAINT Reporte_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Respuesta_Estudiante (table: Respuesta)
ALTER TABLE Respuesta ADD CONSTRAINT Respuesta_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Respuesta_Pregunta (table: Respuesta)
ALTER TABLE Respuesta ADD CONSTRAINT Respuesta_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Soporte_Tipo_Problema (table: Soporte)
ALTER TABLE Soporte ADD CONSTRAINT Soporte_Tipo_Problema
    FOREIGN KEY (Tipo_Problema_id_problema)
    REFERENCES Tipo_Problema (id_problema)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Soporte_Usuario (table: Soporte)
ALTER TABLE Soporte ADD CONSTRAINT Soporte_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Usuario_Contrasenia (table: Usuario)
ALTER TABLE Usuario ADD CONSTRAINT Usuario_Contrasenia
    FOREIGN KEY (Contrasenia_id_pass)
    REFERENCES Contrasenia (id_pass)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Usuario_Roles (table: Usuario)
ALTER TABLE Usuario ADD CONSTRAINT Usuario_Roles
    FOREIGN KEY (Roles_id_rol)
    REFERENCES Roles (id_rol)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- 1) Quitar la PK actual basada solo en id_pass
ALTER TABLE H_Contrasenia DROP CONSTRAINT IF EXISTS H_Contrasenia_pk;

-- 2) Crear PK compuesta
ALTER TABLE H_Contrasenia
  ADD CONSTRAINT H_Contrasenia_pk PRIMARY KEY (id_pass, tx_date);

-- 3) (opcional) Index para consultas por id_pass/TX_DATE
CREATE INDEX IF NOT EXISTS idx_hc_txdate ON H_Contrasenia (id_pass, tx_date DESC);

-- Insert default security configuration
INSERT INTO configuracion_seguridad (
    min_longitud_contrasenia, 
    max_intentos_login, 
    dias_expiracion_contrasenia, 
    meses_no_reutilizar,
    requerir_mayusculas, 
    requerir_minusculas, 
    requerir_numeros, 
    requerir_simbolos,
    fecha_modificacion,
    usuario_modificacion,
    activa
) VALUES (
    12,    -- Minimum password length
    3,     -- Maximum login attempts  
    60,    -- Password expiry in days
    12,    -- Months before password can be reused
    true,  -- Require uppercase letters
    true,  -- Require lowercase letters 
    true,  -- Require numbers
    true,  -- Require special characters
    CURRENT_TIMESTAMP,
    1,     -- Default user ID (system)
    true   -- Configuration is active
);

-- Reference: Log_Usuario_Usuario (table: Log_Usuario)
ALTER TABLE Log_Usuario ADD CONSTRAINT Log_Usuario_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE;

ALTER TABLE Contrasenia DROP COLUMN IF EXISTS contrasenia_id_pass;

-- ===============================================
-- MÓDULO DE ANÁLISIS DE RIESGOS
-- Sistema de Gestión de Riesgos de Seguridad
-- ===============================================

-- Eliminar tablas existentes si existen (en orden correcto por dependencias)
DROP TABLE IF EXISTS historial_kri CASCADE;
DROP TABLE IF EXISTS accion_mitigacion CASCADE;
DROP TABLE IF EXISTS riesgo_evento CASCADE;
DROP TABLE IF EXISTS indicador_riesgo CASCADE;

-- Tabla para almacenar eventos de riesgo
CREATE TABLE riesgo_evento (
    id_riesgo SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    
    -- Análisis de riesgo
    probabilidad INTEGER NOT NULL CHECK (probabilidad >= 1 AND probabilidad <= 5),
    impacto INTEGER NOT NULL CHECK (impacto >= 1 AND impacto <= 5),
    nivel_riesgo VARCHAR(20) NOT NULL,
    valor_riesgo INTEGER NOT NULL,
    
    -- Consecuencias y mitigación
    consecuencias TEXT NOT NULL,
    plan_accion TEXT NOT NULL,
    
    -- Información administrativa
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_identificacion DATE NOT NULL,
    responsable VARCHAR(150) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Identificado',
    
    -- Auditoría
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(100) NOT NULL
);

-- Tabla para indicadores clave de riesgo (KRI)
CREATE TABLE indicador_riesgo (
    id_indicador SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo_indicador VARCHAR(100) NOT NULL,
    umbral_critico DECIMAL(10,2) NOT NULL,
    umbral_advertencia DECIMAL(10,2) NOT NULL,
    valor_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(50) NOT NULL,
    frecuencia_medicion VARCHAR(50) NOT NULL,
    
    -- Estado del indicador
    estado_actual VARCHAR(20) NOT NULL DEFAULT 'Normal',
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    usuario_creacion VARCHAR(100) NOT NULL
);

-- Tabla para historial de mediciones de KRI
CREATE TABLE historial_kri (
    id_medicion SERIAL PRIMARY KEY,
    id_indicador INTEGER NOT NULL REFERENCES indicador_riesgo(id_indicador),
    valor_medido DECIMAL(10,2) NOT NULL,
    fecha_medicion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_evaluado VARCHAR(20) NOT NULL,
    observaciones TEXT,
    usuario_medicion VARCHAR(100) NOT NULL
);

-- Tabla para acciones de mitigación
CREATE TABLE accion_mitigacion (
    id_accion SERIAL PRIMARY KEY,
    id_riesgo INTEGER NOT NULL REFERENCES riesgo_evento(id_riesgo),
    descripcion_accion TEXT NOT NULL,
    responsable_accion VARCHAR(150) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    fecha_completada DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    efectividad VARCHAR(20),
    observaciones TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_riesgo_estado ON riesgo_evento(estado);
CREATE INDEX idx_riesgo_nivel ON riesgo_evento(nivel_riesgo);
CREATE INDEX idx_riesgo_fecha ON riesgo_evento(fecha_identificacion);
CREATE INDEX idx_indicador_activo ON indicador_riesgo(activo);
CREATE INDEX idx_historial_kri_fecha ON historial_kri(fecha_medicion);
CREATE INDEX idx_accion_estado ON accion_mitigacion(estado);

-- Insertar 2 indicadores clave de riesgo predefinidos
INSERT INTO indicador_riesgo 
(nombre, descripcion, tipo_indicador, umbral_critico, umbral_advertencia, valor_actual, unidad_medida, frecuencia_medicion, usuario_creacion)
VALUES 
('Intentos de Acceso No Autorizados', 
 'Número de intentos fallidos de inicio de sesión que exceden el límite establecido en las políticas de seguridad',
 'Seguridad',
 50.00,
 30.00,
 0.00,
 'Intentos por día',
 'Diario',
 'SYSTEM'),
 
('Contraseñas No Conformes con Política', 
 'Porcentaje de contraseñas de usuarios que no cumplen con los requisitos actuales de política de seguridad',
 'Cumplimiento',
 25.00,
 15.00,
 0.00,
 'Porcentaje',
 'Semanal',
 'SYSTEM');

-- Insertar 2 riesgos de ejemplo relacionados al sistema
INSERT INTO riesgo_evento 
(titulo, descripcion, categoria, probabilidad, impacto, nivel_riesgo, valor_riesgo, consecuencias, plan_accion, fecha_identificacion, responsable, usuario_registro)
VALUES 
('Acceso No Autorizado por Fuerza Bruta',
 'Riesgo de que atacantes externos intenten acceder al sistema mediante múltiples intentos de inicio de sesión con diferentes combinaciones de contraseñas',
 'Seguridad de Acceso',
 4,
 5,
 'Crítico',
 20,
 'Compromiso de cuentas de usuario, acceso a información confidencial de estudiantes, modificación no autorizada de datos académicos, pérdida de integridad del sistema',
 'Implementar bloqueo temporal de cuenta después de 3 intentos fallidos, activar autenticación de dos factores (2FA), monitoreo en tiempo real de intentos fallidos, implementar CAPTCHA después del segundo intento fallido',
 CURRENT_DATE,
 'Administrador de Seguridad',
 'SYSTEM'),
 
('Contraseñas Débiles y No Conformes',
 'Riesgo de que usuarios utilicen contraseñas que no cumplan con las políticas de seguridad establecidas, facilitando ataques de diccionario o ingeniería social',
 'Gestión de Contraseñas',
 3,
 4,
 'Alto',
 12,
 'Vulnerabilidad ante ataques de diccionario, compromiso fácil de cuentas de usuario, acceso no autorizado a información sensible, incumplimiento de normativas de seguridad',
 'Forzar actualización de contraseñas cuando cambien las políticas de seguridad, implementar verificación de contraseñas comprometidas en bases de datos públicas, educación continua sobre seguridad de contraseñas, auditorías periódicas de cumplimiento',
 CURRENT_DATE,
 'Administrador de Seguridad',
 'SYSTEM');

-- End of file.

