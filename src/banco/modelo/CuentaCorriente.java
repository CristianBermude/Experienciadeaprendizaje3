package banco.modelo;

import java.sql.Timestamp;

/**
 * Subclase que representa una Cuenta Corriente.
 * Orientada a operaciones comerciales con disponibilidad inmediata.
 */
public class CuentaCorriente extends Cuenta {
    private static final long serialVersionUID = 1L;

    public CuentaCorriente() {
        super();
    }

    public CuentaCorriente(int idCuenta, String numeroCuenta, int idCliente, double saldo,
                           EstadoCuenta estado, Timestamp fechaApertura) {
        super(idCuenta, numeroCuenta, idCliente, saldo, estado, fechaApertura);
    }

    public CuentaCorriente(String numeroCuenta, int idCliente, double saldo) {
        super(0, numeroCuenta, idCliente, saldo, EstadoCuenta.ACTIVA, null);
    }

    @Override
    public TipoCuenta getTipoCuenta() {
        return TipoCuenta.CORRIENTE;
    }

    @Override
    public double calcularRendimientoEstimadoMensual() {
        // En cuenta corriente estándar el rendimiento es 0.00
        return 0.00;
    }
}
