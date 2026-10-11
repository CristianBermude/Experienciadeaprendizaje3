package banco.modelo;

public enum EstadoCliente {
    ACTIVO,
    INACTIVO,
    BLOQUEADO;

    public static EstadoCliente desdeString(String valor) {
        if (valor == null) return ACTIVO;
        try {
            return EstadoCliente.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVO;
        }
    }
}
