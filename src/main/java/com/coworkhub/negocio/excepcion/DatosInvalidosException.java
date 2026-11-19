package com.coworkhub.negocio.excepcion;

/** Datos de entrada incoherentes que no detecta Bean Validation (se traduce a 400). */
public class DatosInvalidosException extends NegocioException {

    public DatosInvalidosException(String mensaje) {
        super("VALIDACION", mensaje);
    }
}
