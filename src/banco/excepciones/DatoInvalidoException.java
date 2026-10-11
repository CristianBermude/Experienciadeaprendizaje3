package banco.excepciones;

public class DatoInvalidoException extends BancoException {
    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
