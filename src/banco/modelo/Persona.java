package banco.modelo;

import java.io.Serializable;

/**
 * Clase base abstracta que modela los atributos comunes de una persona en el sistema bancario.
 * Demuestra el principio de Herencia y Encapsulamiento en POO.
 */
public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String nombreCompleto;
    protected String telefono;
    protected String correoElectronico;
    protected String direccion;

    public Persona() {
    }

    public Persona(int id, String nombreCompleto, String telefono, String correoElectronico, String direccion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + telefono + ")";
    }
}
