package banco.excepciones;

/**
 * Excepción lanzada cuando una cuenta no dispone del saldo requerido para un débito o transferencia.
 */
public class FondosInsuficientesException extends BancoException {
    private static final long serialVersionUID = 1L;

    private final double saldoDisponible;
    private final double montoRequerido;

    public FondosInsuficientesException(double saldoDisponible, double montoRequerido) {
        super(String.format("Fondos insuficientes: Saldo disponible ($%.2f) es menor que el monto requerido ($%.2f).",
                saldoDisponible, montoRequerido));
        this.saldoDisponible = saldoDisponible;
        this.montoRequerido = montoRequerido;
    }

    public FondosInsuficientesException(String mensaje) {
        super(mensaje);
        this.saldoDisponible = 0.0;
        this.montoRequerido = 0.0;
    }

    public double getSaldoDisponible() {
        return saldoDisponible;
    }

    public double getMontoRequerido() {
        return montoRequerido;
    }
}

