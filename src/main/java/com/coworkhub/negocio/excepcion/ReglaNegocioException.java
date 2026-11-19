package com.coworkhub.negocio.excepcion;

/** Se viola una regla de negocio RN-xx o hay un duplicado (se traduce a 409). */
public class ReglaNegocioException extends NegocioException {

    public ReglaNegocioException(String regla, String mensaje) {
        super(regla, mensaje);
    }
}
