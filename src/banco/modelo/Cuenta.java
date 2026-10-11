package banco.modelo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Clase base abstracta para Cuentas Bancarias.
 * Define la estructura general de una cuenta y métodos polimórficos de negocio.
 */
public abstract class Cuenta implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int idCuenta;
    protected String numeroCuenta;
    protected int idCliente;
    protected Cliente cliente;
    protected double saldo;
    protected EstadoCuenta estado;
    protected Timestamp fechaApertura;

    public Cuenta() {
        this.estado = EstadoCuenta.ACTIVA;
        this.saldo = 0.0;
    }

    public Cuenta(int idCuenta, String numeroCuenta, int idCliente, double saldo,
                  EstadoCuenta estado, Timestamp fechaApertura) {
        this.idCuenta = idCuenta;
        this.numeroCuenta = numeroCuenta;
        this.idCliente = idCliente;
        this.saldo = saldo;
        this.estado = (estado != null) ? estado : EstadoCuenta.ACTIVA;
        this.fechaApertura = fechaApertura;
    }

    public abstract TipoCuenta getTipoCuenta();

    /**
     * Calcula una tasa o beneficio simulado de acuerdo al tipo de cuenta.
     * Demuestra polimorfismo.
     */
    public abstract double calcularRendimientoEstimadoMensual();

    public boolean puedeOperar() {
        return this.estado == EstadoCuenta.ACTIVA;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getId();
        }
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    public Timestamp getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Timestamp fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    @Override
    public String toString() {
        return numeroCuenta + " [" + getTipoCuenta() + "] - Saldo: $" + String.format("%.2f", saldo);
    }
}
