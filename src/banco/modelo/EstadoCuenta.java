package banco.modelo;

public enum EstadoCuenta {
    ACTIVA,
    BLOQUEADA,
    CANCELADA;

    public static EstadoCuenta desdeString(String valor) {
        if (valor == null) return ACTIVA;
        try {
            return EstadoCuenta.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVA;
        }
    }
}
