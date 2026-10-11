package banco.excepciones;

public class SaldoInvalidoException extends BancoException {
    private static final long serialVersionUID = 1L;

    public SaldoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
