package banco.excepciones;

public class ClienteNoEncontradoException extends BancoException {
    private static final long serialVersionUID = 1L;

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
