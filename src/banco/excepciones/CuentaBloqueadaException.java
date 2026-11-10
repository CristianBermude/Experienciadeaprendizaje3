package banco.excepciones;

/**
 * Excepción lanzada cuando se intenta realizar una transacción sobre una cuenta bancaria bloqueada o inactiva.
 */
public class CuentaBloqueadaException extends BancoException {
    private static final long serialVersionUID = 1L;

    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}

