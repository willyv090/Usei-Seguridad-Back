package com.usei.usei;

import com.usei.usei.util.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**") // Aplicar a todas las rutas
                .excludePathPatterns(
                    // ======================================
                    // AUTENTICACIÓN
                    // ======================================
                    "/auth/**",
                    
                    // ======================================
                    // MÓDULO DE SEGURIDAD (SIN TOKEN)
                    // ======================================
                    "/usuario/**",      // ⭐ Gestión de usuarios
                    "/rol/**",          // ⭐ Gestión de roles
                    
                    // ======================================
                    // RECUPERACIÓN DE CONTRASEÑAS
                    // ======================================
                    "/estudiante/enviarCodigoVerificacion/**",
                    "/usuario/enviarCodigoVerificacion",
                    
                    // ======================================
                    // RECURSOS PÚBLICOS
                    // ======================================
                    "/noticia/carrusel",
                    "/documents/**",
                    "/imagenes/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:src/main/resources/static/documents/imagenes/");
    }
}