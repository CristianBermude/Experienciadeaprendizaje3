package banco.excepciones;

public class CuentaNoDisponibleException extends BancoException {
    private static final long serialVersionUID = 1L;

    public CuentaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
