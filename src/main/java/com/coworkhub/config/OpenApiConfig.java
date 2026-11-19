package com.coworkhub.config;

import com.coworkhub.presentacion.excepcion.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Swagger UI en /doc/swagger-ui.html: grupos por area, esquema Bearer (boton Authorize) y las respuestas
 * de error estandar (con ejemplos) agregadas a todas las operaciones.
 */
@Configuration
public class OpenApiConfig {

    static final String ESQUEMA_SEGURIDAD = "bearerAuth";
    private static final String ESQUEMA_ERROR = "ErrorResponse";

    @Bean
    public OpenAPI coworkhubOpenApi() {
        Components componentes = new Components()
                .addSecuritySchemes(ESQUEMA_SEGURIDAD, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Obtenga el token con POST /auth/login y peguelo aqui (sin el prefijo Bearer)."));
        ModelConverters.getInstance().read(ErrorResponse.class).forEach(componentes::addSchemas);
        return new OpenAPI()
                .info(new Info()
                        .title("CoWorkHub API")
                        .version("1.0.0")
                        .description("""
                                API REST para gestionar las reservas de una red de coworking: sedes, salas, \
                                equipamiento, servicios adicionales, planes, miembros, disponibilidad y reservas.

                                **Como probarla:** 1) `POST /auth/login` con un usuario de prueba \
                                (admin/admin123, recepcion/recep123, ana/ana123, luis/luis123). \
                                2) Boton **Authorize** y pegar el token. El token vence a los 60 minutos.

                                Todo error responde `{"error", "codigo", "timestamp"}`, donde `codigo` es la regla \
                                de negocio violada (RN-xx) o el tipo de error.""")
                        .contact(new Contact().name("CoWorkHub - Proyecto final Spring Boot")))
                .components(componentes)
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }

    @Bean
    public GroupedOpenApi grupoTodos() {
        return grupo("0-todos", "/**");
    }

    @Bean
    public GroupedOpenApi grupoAutenticacion() {
        return grupo("1-autenticacion-plataforma", "/auth/**", "/actuator/**");
    }

    @Bean
    public GroupedOpenApi grupoCatalogos() {
        return grupo("2-catalogos", "/api/sedes/**", "/api/salas/**", "/api/equipamientos/**",
                "/api/servicios-adicionales/**", "/api/planes/**");
    }

    @Bean
    public GroupedOpenApi grupoMiembros() {
        return grupo("3-miembros", "/api/miembros/**");
    }

    @Bean
    public GroupedOpenApi grupoReservas() {
        return grupo("4-reservas-disponibilidad", "/api/reservas/**", "/api/disponibilidad/**");
    }

    private GroupedOpenApi grupo(String nombre, String... rutas) {
        return GroupedOpenApi.builder()
                .group(nombre)
                .pathsToMatch(rutas)
                .addOpenApiCustomizer(respuestasDeError())
                .build();
    }

    /** Agrega a cada operacion las respuestas de error con el cuerpo estandar y un ejemplo. */
    @Bean
    public OpenApiCustomizer respuestasDeError() {
        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null
                    && !openApi.getComponents().getSchemas().containsKey(ESQUEMA_ERROR)) {
                ModelConverters.getInstance().read(ErrorResponse.class).forEach(openApi.getComponents()::addSchemas);
            }
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((ruta, item) -> item.readOperationsMap().forEach((metodo, op) -> {
                boolean publica = ruta.startsWith("/auth/login") || ruta.startsWith("/actuator");
                if (ruta.startsWith("/actuator")) {
                    op.setSecurity(java.util.List.of());
                    return;
                }
                agregar(op, "400", "Datos faltantes o con formato invalido", "VALIDACION", "nombre: El nombre es obligatorio");
                if (publica) {
                    agregar(op, "401", "Credenciales invalidas", "NO_AUTENTICADO", "Usuario o contrasena incorrectos");
                    return;
                }
                agregar(op, "401", "Sin token, o token alterado o vencido", "NO_AUTENTICADO", "El token es invalido o fue alterado");
                agregar(op, "403", "El usuario no tiene permiso", "ACCESO_DENEGADO", "No tiene permiso para realizar esta operacion");
                if (ruta.contains("{")) {
                    agregar(op, "404", "El recurso no existe", "NO_ENCONTRADO", "Reserva con id 99 no existe");
                }
                if (!"GET".equals(metodo.name()) || ruta.contains("disponibilidad")) {
                    agregar(op, "409", "Se viola una regla de negocio o hay un duplicado", "RN-01", "La sala ya esta reservada en ese horario");
                }
            }));
        };
    }

    private static void agregar(Operation op, String codigo, String descripcion, String codigoError, String mensaje) {
        ApiResponses respuestas = op.getResponses() == null ? new ApiResponses() : op.getResponses();
        if (respuestas.containsKey(codigo)) {
            return;
        }
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("error", mensaje);
        ejemplo.put("codigo", codigoError);
        ejemplo.put("timestamp", "2030-06-03T13:00:00Z");
        MediaType json = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + ESQUEMA_ERROR))
                .addExamples(codigoError, new Example().value(ejemplo));
        respuestas.addApiResponse(codigo, new ApiResponse().description(descripcion)
                .content(new Content().addMediaType("application/json", json)));
        op.setResponses(respuestas);
    }
}
