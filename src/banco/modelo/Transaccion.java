package banco.modelo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Entidad que modela una Transacción Bancaria (Depósito, Retiro o Transferencia).
 * Compatible con la especificación JavaBean para persistencia y presentación.
 */
public class Transaccion implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tipo {
        DEPOSITO("Depósito"),
        RETIRO("Retiro"),
        TRANSFERENCIA("Transferencia");

        private final String nombreVisual;

        Tipo(String nombreVisual) {
            this.nombreVisual = nombreVisual;
        }

        public String getNombreVisual() {
            return nombreVisual;
        }

        public static Tipo desdeString(String valor) {
            if (valor == null) return DEPOSITO;
            try {
                return Tipo.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return DEPOSITO;
            }
        }
    }

    private int idTransaccion;
    private String codigoTransaccion;
    private int idCuentaOrigen;
    private Integer idCuentaDestino;
    private Tipo tipo;
    private double monto;
    private double saldoResultante;
    private String descripcion;
    private Timestamp fechaHora;

    // Atributos de navegación / presentación para vistas y reportes
    private String numeroCuentaOrigen;
    private String numeroCuentaDestino;
    private String titularOrigen;
    private String titularDestino;

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

    public String getNumeroCuentaOrigen() { return numeroCuentaOrigen; }
    public void setNumeroCuentaOrigen(String numeroCuentaOrigen) { this.numeroCuentaOrigen = numeroCuentaOrigen; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public String getTitularOrigen() { return titularOrigen; }
    public void setTitularOrigen(String titularOrigen) { this.titularOrigen = titularOrigen; }

    public String getTitularDestino() { return titularDestino; }
    public void setTitularDestino(String titularDestino) { this.titularDestino = titularDestino; }

    @Override
    public String toString() {
        return codigoTransaccion + " [" + tipo + "] $" + String.format("%.2f", monto) +
               " (Origen: " + numeroCuentaOrigen + (numeroCuentaDestino != null ? " -> Destino: " + numeroCuentaDestino : "") + ")";
    }
}
