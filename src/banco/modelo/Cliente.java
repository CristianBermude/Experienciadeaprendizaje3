package banco.modelo;

import java.sql.Timestamp;

/**
 * Entidad que representa a un Cliente del Banco Académico UDB.
 * Extiende de Persona demostrando Herencia, Encapsulamiento y cumplimiento con JavaBean.
 */
public class Cliente extends Persona {
    private static final long serialVersionUID = 1L;

    private String codigoCliente;
    private String dui;
    private EstadoCliente estado;
    private Timestamp fechaRegistro;

    public Cliente() {
        super();
        this.estado = EstadoCliente.ACTIVO;
    }

    public Cliente(int id, String codigoCliente, String dui, String nombreCompleto,
                   String telefono, String correoElectronico, String direccion,
                   EstadoCliente estado, Timestamp fechaRegistro) {
        super(id, nombreCompleto, telefono, correoElectronico, direccion);
        this.codigoCliente = codigoCliente;
        this.dui = dui;
        this.estado = (estado != null) ? estado : EstadoCliente.ACTIVO;
        this.fechaRegistro = fechaRegistro;
    }

    public Cliente(String codigoCliente, String dui, String nombreCompleto,
                   String telefono, String correoElectronico, String direccion,
                   EstadoCliente estado) {
        this(0, codigoCliente, dui, nombreCompleto, telefono, correoElectronico, direccion, estado, null);
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return this.estado == EstadoCliente.ACTIVO;
    }

    public boolean isBloqueado() {
        return this.estado == EstadoCliente.BLOQUEADO;
    }

    @Override
    public String toString() {
        return codigoCliente + " - " + nombreCompleto + " (DUI: " + dui + ")";
    }
}
