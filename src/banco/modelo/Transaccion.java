package banco.modelo;

import java.io.Serializable;
import java.sql.Timestamp;

public class Transaccion implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tipo { DEPOSITO, RETIRO, TRANSFERENCIA }

    private int idTransaccion;
    private String codigoTransaccion;
    private int idCuentaOrigen;
    private Integer idCuentaDestino;
    private Tipo tipo;
    private double monto;
    private double saldoResultante;
    private String descripcion;
    private Timestamp fechaHora;

    public Transaccion() {}

    public Transaccion(int idTransaccion, String codigoTransaccion, int idCuentaOrigen,
                       Integer idCuentaDestino, Tipo tipo, double monto,
                       double saldoResultante, String descripcion, Timestamp fechaHora) {
        this.idTransaccion = idTransaccion;
        this.codigoTransaccion = codigoTransaccion;
        this.idCuentaOrigen = idCuentaOrigen;
        this.idCuentaDestino = idCuentaDestino;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
    }

    public int getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(int idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getCodigoTransaccion() { return codigoTransaccion; }
    public void setCodigoTransaccion(String codigoTransaccion) { this.codigoTransaccion = codigoTransaccion; }

    public int getIdCuentaOrigen() { return idCuentaOrigen; }
    public void setIdCuentaOrigen(int idCuentaOrigen) { this.idCuentaOrigen = idCuentaOrigen; }

    public Integer getIdCuentaDestino() { return idCuentaDestino; }
    public void setIdCuentaDestino(Integer idCuentaDestino) { this.idCuentaDestino = idCuentaDestino; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public double getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(double saldoResultante) { this.saldoResultante = saldoResultante; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }
}
