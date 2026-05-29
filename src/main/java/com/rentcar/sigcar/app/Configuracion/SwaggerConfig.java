package com.rentcar.sigcar.app.Configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI sigcarOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SIGCAR API — Sistema Integral de Gestión de Carros")
                .description(
                    "API REST para la gestión completa de alquiler de vehículos. " +
                    "RentCar Express S.A.S. — Bucaramanga, Colombia. " +
                    "Desarrollado con Spring Boot 3.2.5 + MongoDB Atlas.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Juan Daniel Miranda Palmera")
                    .email("contacto@rentcarexpress.com"))
                .license(new License()
                    .name("UTS — Desarrollo de Aplicaciones Empresariales")
                    .url("https://www.uts.edu.co")))
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                    .name("JWT")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Ingresa el token JWT obtenido al hacer login")));
    }
}