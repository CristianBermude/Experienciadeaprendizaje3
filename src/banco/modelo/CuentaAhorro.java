package banco.modelo;

import java.sql.Timestamp;

/**
 * Subclase que representa una Cuenta de Ahorro.
 * Genera intereses sobre el saldo disponible.
 */
public class CuentaAhorro extends Cuenta {
    private static final long serialVersionUID = 1L;

    public CuentaAhorro() {
        super();
    }

    public CuentaAhorro(int idCuenta, String numeroCuenta, int idCliente, double saldo,
                        EstadoCuenta estado, Timestamp fechaApertura) {
        super(idCuenta, numeroCuenta, idCliente, saldo, estado, fechaApertura);
    }

    public CuentaAhorro(String numeroCuenta, int idCliente, double saldo) {
        super(0, numeroCuenta, idCliente, saldo, EstadoCuenta.ACTIVA, null);
    }

    @Override
    public TipoCuenta getTipoCuenta() {
        return TipoCuenta.AHORRO;
    }

    @Override
    public double calcularRendimientoEstimadoMensual() {
        // Tasa anual del 2.0% dividida en 12 meses
        return (this.saldo * TipoCuenta.AHORRO.getTasaInteresAnual()) / 12.0;
    }
}
