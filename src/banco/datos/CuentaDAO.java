package banco.datos;

import banco.excepciones.BancoException;
import banco.modelo.Cuenta;
import banco.modelo.CuentaAhorro;
import banco.modelo.CuentaCorriente;
import banco.modelo.EstadoCuenta;
import banco.modelo.TipoCuenta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Cuenta Bancaria y sus subclases polimórficas.
 */
public class CuentaDAO {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public int insertar(Cuenta cuenta) throws BancoException {
        String sql = "INSERT INTO cuentas (numero_cuenta, id_cliente, tipo_cuenta, saldo, estado) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cuenta.getNumeroCuenta().trim());
            stmt.setInt(2, cuenta.getIdCliente());
            stmt.setString(3, cuenta.getTipoCuenta().name());
            stmt.setDouble(4, cuenta.getSaldo());
            stmt.setString(5, cuenta.getEstado().name());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    cuenta.setIdCuenta(idGenerado);
                    return idGenerado;
                }
            }
            return 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new BancoException("Ya existe una cuenta bancaria con el número: " + cuenta.getNumeroCuenta());
            } else if (e.getErrorCode() == 1452) {
                throw new BancoException("El cliente con ID " + cuenta.getIdCliente() + " no existe en el sistema.");
            }
            throw new BancoException("Error al registrar cuenta bancaria: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public boolean actualizarEstado(int idCuenta, EstadoCuenta nuevoEstado) throws BancoException {
        String sql = "UPDATE cuentas SET estado = ? WHERE id_cuenta = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nuevoEstado.name());
            stmt.setInt(2, idCuenta);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BancoException("Error al cambiar estado de la cuenta: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, null);
        }
    }

    public boolean actualizarSaldo(int idCuenta, double nuevoSaldo) throws BancoException {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nuevoSaldo);
            stmt.setInt(2, idCuenta);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BancoException("Error al actualizar saldo de la cuenta: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, null);
        }
    }

    public Cuenta buscarPorId(int idCuenta) throws BancoException {
        String sql = "SELECT * FROM cuentas WHERE id_cuenta = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCuenta);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearCuenta(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar cuenta por ID: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public Cuenta buscarPorNumero(String numeroCuenta) throws BancoException {
        String sql = "SELECT * FROM cuentas WHERE numero_cuenta = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, numeroCuenta.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearCuenta(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar cuenta por número: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public List<Cuenta> listarTodas() throws BancoException {
        String sql = "SELECT * FROM cuentas ORDER BY id_cuenta DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Cuenta> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearCuenta(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al listar todas las cuentas: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public List<Cuenta> listarPorCliente(int idCliente) throws BancoException {
        String sql = "SELECT * FROM cuentas WHERE id_cliente = ? ORDER BY id_cuenta DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Cuenta> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearCuenta(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al listar cuentas por cliente: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    public double obtenerTotalSaldosGlobal() throws BancoException {
        String sql = "SELECT COALESCE(SUM(saldo), 0) FROM cuentas";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new BancoException("Error al calcular saldo total: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    private Cuenta mapearCuenta(ResultSet rs) throws SQLException, BancoException {
        String tipoStr = rs.getString("tipo_cuenta");
        TipoCuenta tipo = TipoCuenta.desdeString(tipoStr);

        Cuenta cuenta;
        if (tipo == TipoCuenta.AHORRO) {
            cuenta = new CuentaAhorro();
        } else {
            cuenta = new CuentaCorriente();
        }

        cuenta.setIdCuenta(rs.getInt("id_cuenta"));
        cuenta.setNumeroCuenta(rs.getString("numero_cuenta"));
        int idCli = rs.getInt("id_cliente");
        cuenta.setIdCliente(idCli);
        cuenta.setSaldo(rs.getDouble("saldo"));
        cuenta.setEstado(EstadoCuenta.desdeString(rs.getString("estado")));
        cuenta.setFechaApertura(rs.getTimestamp("fecha_apertura"));

        // Vincular cliente para navegación de objetos
        try {
            cuenta.setCliente(clienteDAO.buscarPorId(idCli));
        } catch (Exception ignored) {}

        return cuenta;
    }
}
