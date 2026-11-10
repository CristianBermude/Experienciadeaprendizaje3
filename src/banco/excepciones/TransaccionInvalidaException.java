package banco.excepciones;

/**
 * Excepción lanzada cuando los parámetros de una transacción bancaria violan las reglas del negocio.
 */
public class TransaccionInvalidaException extends BancoException {
    private static final long serialVersionUID = 1L;

    public TransaccionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public TransaccionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

