package banco.datos;

import banco.excepciones.BancoException;
import banco.modelo.Cliente;
import banco.modelo.EstadoCliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Cliente.
 * Manejo robusto de persistencia mediante PreparedStatement.
 */
public class ClienteDAO {

    public int insertar(Cliente cliente) throws BancoException {
        String sql = "INSERT INTO clientes (codigo_cliente, dui, nombre_completo, telefono, correo_electronico, direccion, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cliente.getCodigoCliente().trim());
            stmt.setString(2, cliente.getDui().trim());
            stmt.setString(3, cliente.getNombreCompleto().trim());
            stmt.setString(4, cliente.getTelefono().trim());
            stmt.setString(5, cliente.getCorreoElectronico().trim());
            stmt.setString(6, cliente.getDireccion().trim());
            stmt.setString(7, cliente.getEstado().name());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    cliente.setId(idGenerado);
                    return idGenerado;
                }
            }
            return 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                if (e.getMessage().contains("dui")) {
                    throw new BancoException("Ya existe un cliente registrado con el DUI: " + cliente.getDui());
                } else if (e.getMessage().contains("codigo_cliente")) {
                    throw new BancoException("Ya existe un cliente registrado con el código: " + cliente.getCodigoCliente());
                }
            }
            throw new BancoException("Error al insertar cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public boolean actualizar(Cliente cliente) throws BancoException {
        String sql = "UPDATE clientes SET codigo_cliente = ?, dui = ?, nombre_completo = ?, " +
                     "telefono = ?, correo_electronico = ?, direccion = ?, estado = ? WHERE id_cliente = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cliente.getCodigoCliente().trim());
            stmt.setString(2, cliente.getDui().trim());
            stmt.setString(3, cliente.getNombreCompleto().trim());
            stmt.setString(4, cliente.getTelefono().trim());
            stmt.setString(5, cliente.getCorreoElectronico().trim());
            stmt.setString(6, cliente.getDireccion().trim());
            stmt.setString(7, cliente.getEstado().name());
            stmt.setInt(8, cliente.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                if (e.getMessage().contains("dui")) {
                    throw new BancoException("El DUI " + cliente.getDui() + " ya pertenece a otro cliente.");
                } else if (e.getMessage().contains("codigo_cliente")) {
                    throw new BancoException("El código " + cliente.getCodigoCliente() + " ya está asignado a otro cliente.");
                }
            }
            throw new BancoException("Error al actualizar cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, null);
        }
    }

    public boolean eliminar(int idCliente) throws BancoException {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) { // Foreign key constraint fails
                throw new BancoException("No se puede eliminar el cliente porque tiene cuentas bancarias vinculadas. Elimine o cancele las cuentas primero.");
            }
            throw new BancoException("Error al eliminar cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, null);
        }
    }

    public boolean tieneCuentasAsociadas(int idCliente) throws BancoException {
        String sql = "SELECT COUNT(*) FROM cuentas WHERE id_cliente = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new BancoException("Error al verificar cuentas del cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public Cliente buscarPorId(int idCliente) throws BancoException {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        return ejecutarConsultaUnica(sql, idCliente);
    }

    public Cliente buscarPorCodigo(String codigo) throws BancoException {
        String sql = "SELECT * FROM clientes WHERE UPPER(codigo_cliente) = UPPER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, codigo.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar cliente por código: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public Cliente buscarPorDui(String dui) throws BancoException {
        String sql = "SELECT * FROM clientes WHERE dui = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, dui.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar cliente por DUI: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public List<Cliente> listarTodos() throws BancoException {
        String sql = "SELECT * FROM clientes ORDER BY id_cliente DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Cliente> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al listar clientes: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public List<Cliente> buscarPorTermino(String termino) throws BancoException {
        String sql = "SELECT * FROM clientes WHERE codigo_cliente LIKE ? OR dui LIKE ? OR nombre_completo LIKE ? OR correo_electronico LIKE ? ORDER BY id_cliente DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Cliente> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            String parametro = "%" + termino.trim() + "%";
            stmt.setString(1, parametro);
            stmt.setString(2, parametro);
            stmt.setString(3, parametro);
            stmt.setString(4, parametro);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al filtrar clientes: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    private Cliente ejecutarConsultaUnica(String sql, int parametro) throws BancoException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, parametro);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error en consulta única de cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id_cliente"));
        c.setCodigoCliente(rs.getString("codigo_cliente"));
        c.setDui(rs.getString("dui"));
        c.setNombreCompleto(rs.getString("nombre_completo"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreoElectronico(rs.getString("correo_electronico"));
        c.setDireccion(rs.getString("direccion"));
        c.setEstado(EstadoCliente.desdeString(rs.getString("estado")));
        c.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return c;
    }
}
