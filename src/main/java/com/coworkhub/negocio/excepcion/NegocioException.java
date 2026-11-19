package com.coworkhub.negocio.excepcion;

/**
 * Base de las excepciones de la capa de negocio. No conoce HTTP: solo lleva un codigo
 * (la regla violada o el tipo de error) que la capa de presentacion traduce a un estado HTTP.
 */
public abstract class NegocioException extends RuntimeException {

    private final String codigo;

    protected NegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
