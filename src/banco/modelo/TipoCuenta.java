package banco.modelo;

public enum TipoCuenta {
    AHORRO("Cuenta de Ahorro", 0.02),
    CORRIENTE("Cuenta Corriente", 0.00);

    private final String nombreVisual;
    private final double tasaInteresAnual;

    TipoCuenta(String nombreVisual, double tasaInteresAnual) {
        this.nombreVisual = nombreVisual;
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public String getNombreVisual() {
        return nombreVisual;
    }

    public double getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public static TipoCuenta desdeString(String valor) {
        if (valor == null) return AHORRO;
        try {
            return TipoCuenta.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AHORRO;
        }
    }
}
