package banco.excepciones;

public class BancoException extends Exception {
    private static final long serialVersionUID = 1L;

    public BancoException(String mensaje) {
        super(mensaje);
    }

    public BancoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
