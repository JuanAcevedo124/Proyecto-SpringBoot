package com.coworkhub.negocio.excepcion;

/** El recurso pedido no existe (se traduce a 404). */
public class RecursoNoEncontradoException extends NegocioException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("NO_ENCONTRADO", recurso + " con id " + id + " no existe");
    }
}
