package banco.modelo;

import java.io.Serializable;
import java.sql.Timestamp;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Rol {
        ADMINISTRADOR,
        CAJERO,
        CLIENTE;

        public static Rol desdeString(String valor) {
            if (valor == null) return CLIENTE;
            try {
                return Rol.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return CLIENTE;
            }
        }
    }

    private int idUsuario;
    private String username;
    private String passwordHash;
    private String nombreCompleto;
    private Rol rol;
    private Integer idCliente;
    private boolean activo;
    private Timestamp fechaCreacion;

    public Usuario() {}

    public Usuario(int idUsuario, String username, String passwordHash, String nombreCompleto,
                   Rol rol, Integer idCliente, boolean activo, Timestamp fechaCreacion) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.idCliente = idCliente;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
