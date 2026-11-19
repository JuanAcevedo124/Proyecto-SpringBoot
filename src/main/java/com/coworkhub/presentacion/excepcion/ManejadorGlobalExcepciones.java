package com.coworkhub.presentacion.excepcion;

import com.coworkhub.negocio.excepcion.DatosInvalidosException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Unico punto que traduce excepciones a respuestas HTTP. Los servicios lanzan excepciones propias
 * (sin conocer HTTP) y aqui se decide el codigo de estado. Todas las respuestas usan {@link ErrorResponse}.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    private final FabricaErrores errores;

    public ManejadorGlobalExcepciones(FabricaErrores errores) {
        this.errores = errores;
    }

    /* ----------------------------- Excepciones de negocio ----------------------------- */

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getCodigo(), e.getMessage());
    }

    /* ----------------------------- Validacion y formato (400) ----------------------------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .map(campo -> campo + ": " + e.getBindingResult().getFieldErrors(campo).getFirst().getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", detalle.isBlank() ? "Datos invalidos" : detalle);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> validacionParametros(HandlerMethodValidationException e) {
        String detalle = e.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", detalle);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccion(ConstraintViolationException e) {
        String detalle = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "JSON_INVALIDO",
                "El cuerpo de la solicitud falta o no es un JSON valido (revise formatos de fecha y numeros)");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "El parametro '" + e.getName() + "' tiene un formato invalido: " + e.getValue());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingPathVariableException.class})
    public ResponseEntity<ErrorResponse> parametroFaltante(Exception e) {
        String nombre = e instanceof MissingServletRequestParameterException m ? m.getParameterName()
                : ((MissingPathVariableException) e).getVariableName();
        return responder(HttpStatus.BAD_REQUEST, "PARAMETRO_FALTANTE", "Falta el parametro obligatorio '" + nombre + "'");
    }

    /* ----------------------------- Errores de Spring MVC ----------------------------- */

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> rutaInexistente(Exception e) {
        return responder(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA", "La ruta solicitada no existe");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El metodo " + e.getMethod() + " no esta permitido en esta ruta");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tipoContenido(HttpMediaTypeNotSupportedException e) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_CONTENIDO_NO_SOPORTADO",
                "El tipo de contenido debe ser application/json");
    }

    /* ----------------------------- Persistencia ----------------------------- */

    /** Red de seguridad: si una restriccion unica de la base salta (p. ej. dos altas simultaneas), es un duplicado. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        log.warn("Violacion de integridad: {}", e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "RN-11", "El dato ya existe o esta en uso por otro registro");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> bloqueo(PessimisticLockingFailureException e) {
        return responder(HttpStatus.CONFLICT, "RN-01", "La sala se esta reservando en este momento, intente de nuevo");
    }

    /* ----------------------------- Seguridad (cuando falla dentro de un controlador) ----------------------------- */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tiene permiso para realizar esta operacion");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException e) {
        return responder(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Usuario o contrasena incorrectos");
    }

    /* ----------------------------- Inesperados ----------------------------- */

    /** Nunca filtra detalles internos: el detalle va al log, el cliente recibe un mensaje generico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e) {
        log.error("Error inesperado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(errores.crear(codigo, mensaje));
    }
}
