package banco.datos;

import banco.excepciones.BancoException;
import banco.excepciones.CuentaBloqueadaException;
import banco.excepciones.CuentaNoDisponibleException;
import banco.excepciones.FondosInsuficientesException;
import banco.excepciones.TransaccionInvalidaException;
import banco.modelo.EstadoCuenta;
import banco.modelo.Transaccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data Access Object (DAO) para transacciones financieras.
 * Implementa control transaccional ACID estricto (setAutoCommit, commit, rollback)
 * para depósitos, retiros y transferencias atómicas.
 */
public class TransaccionDAO {

    private static final Logger logger = LogManager.getLogger(TransaccionDAO.class);

    private final SimpleDateFormat formatoCodigoFecha = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Revierte la transacción en curso. Si el propio rollback falla, el error se registra
     * en la bitácora (nivel ERROR) en lugar de ignorarse en silencio.
     */
    private static void revertir(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
            logger.warn("ROLLBACK ejecutado: la operación fue revertida y ningún saldo fue modificado.");
        } catch (SQLException e) {
            logger.error("FALLO al ejecutar ROLLBACK; el estado de la transacción es incierto: {}", e.getMessage(), e);
        }
    }

    /** Restaura el modo autocommit antes de devolver la conexión, registrando cualquier fallo. */
    private static void restaurarAutoCommit(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("No se pudo restaurar autocommit en la conexión: {}", e.getMessage(), e);
        }
    }

    /**
     * Contador compartido (por JVM) para el sufijo del código de transacción. Antes se usaba un número
     * aleatorio de 3 cifras: con varias operaciones en el mismo segundo la probabilidad de repetir el
     * código era alta y la restricción UNIQUE de la BD rechazaba la transacción. Ahora el código combina
     * marca de tiempo con milisegundos + contador incremental de 3 cifras, por lo que es único dentro de la
     * misma ejecución y prácticamente imposible de repetir entre ejecuciones distintas.
     */
    private static final java.util.concurrent.atomic.AtomicInteger secuenciaCodigo =
            new java.util.concurrent.atomic.AtomicInteger((int) (Math.random() * 1000));

    private synchronized String generarCodigo(String prefijo) {
        int secuencia = Math.floorMod(secuenciaCodigo.getAndIncrement(), 1000);
        return "TX-" + prefijo + "-" + formatoCodigoFecha.format(new Date()) + "-" + String.format("%03d", secuencia);
    }

    /**
     * Realiza un depósito bancario incrementando el saldo y registrando la transacción.
     */
    public Transaccion realizarDeposito(int idCuenta, double monto, String descripcion) throws BancoException {
        if (monto <= 0) {
            throw new TransaccionInvalidaException("El monto a depositar debe ser mayor a $0.00.");
        }

        Connection conn = null;
        PreparedStatement stmtCuenta = null;
        PreparedStatement stmtUpdate = null;
        PreparedStatement stmtTx = null;
        ResultSet rsCuenta = null;
        ResultSet rsTx = null;

        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción ACID

            // 1. Validar y bloquear la cuenta para actualización
            String sqlCuenta = "SELECT id_cuenta, numero_cuenta, saldo, estado FROM cuentas WHERE id_cuenta = ? FOR UPDATE";
            stmtCuenta = conn.prepareStatement(sqlCuenta);
            stmtCuenta.setInt(1, idCuenta);
            rsCuenta = stmtCuenta.executeQuery();

            if (!rsCuenta.next()) {
                throw new CuentaNoDisponibleException("La cuenta bancaria con ID " + idCuenta + " no existe.");
            }

            String numeroCuenta = rsCuenta.getString("numero_cuenta");
            double saldoActual = rsCuenta.getDouble("saldo");
            EstadoCuenta estado = EstadoCuenta.desdeString(rsCuenta.getString("estado"));

            if (estado != EstadoCuenta.ACTIVA) {
                throw new CuentaBloqueadaException("La cuenta " + numeroCuenta + " está en estado " + estado +
                        " y no permite depósitos.");
            }

            double saldoResultante = saldoActual + monto;

            // 2. Actualizar el saldo
            String sqlUpdate = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";
            stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setDouble(1, saldoResultante);
            stmtUpdate.setInt(2, idCuenta);
            stmtUpdate.executeUpdate();

            // 3. Registrar la transacción
            String codigoTx = generarCodigo("DEP");
            String descFinal = (descripcion != null && !descripcion.trim().isEmpty())
                    ? descripcion.trim()
                    : "Depósito en efectivo a cuenta " + numeroCuenta;

            String sqlTx = "INSERT INTO transacciones (codigo_transaccion, id_cuenta_origen, id_cuenta_destino, " +
                           "tipo_transaccion, monto, saldo_resultante, descripcion) " +
                           "VALUES (?, ?, NULL, 'DEPOSITO', ?, ?, ?)";
            stmtTx = conn.prepareStatement(sqlTx, Statement.RETURN_GENERATED_KEYS);
            stmtTx.setString(1, codigoTx);
            stmtTx.setInt(2, idCuenta);
            stmtTx.setDouble(3, monto);
            stmtTx.setDouble(4, saldoResultante);
            stmtTx.setString(5, descFinal);
            stmtTx.executeUpdate();

            rsTx = stmtTx.getGeneratedKeys();
            int idTx = 0;
            if (rsTx.next()) {
                idTx = rsTx.getInt(1);
            }

            conn.commit(); // Confirmar transacción

            Transaccion tx = new Transaccion(idTx, codigoTx, idCuenta, null, Transaccion.Tipo.DEPOSITO,
                    monto, saldoResultante, descFinal, new java.sql.Timestamp(System.currentTimeMillis()));
            tx.setNumeroCuentaOrigen(numeroCuenta);
            return tx;

        } catch (SQLException e) {
            revertir(conn);
            throw new BancoException("Error en base de datos al realizar depósito: " + e.getMessage(), e);
        } catch (BancoException e) {
            revertir(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
            ConexionBD.cerrar(rsCuenta);
            ConexionBD.cerrar(stmtCuenta);
            ConexionBD.cerrar(stmtUpdate);
            ConexionBD.cerrar(rsTx);
            ConexionBD.cerrar(stmtTx);
            ConexionBD.cerrar(conn);
        }
    }

    /**
     * Realiza un retiro bancario deduciendo el saldo de la cuenta con validación de fondos suficientes.
     */
    public Transaccion realizarRetiro(int idCuenta, double monto, String descripcion) throws BancoException {
        if (monto <= 0) {
            throw new TransaccionInvalidaException("El monto a retirar debe ser mayor a $0.00.");
        }

        Connection conn = null;
        PreparedStatement stmtCuenta = null;
        PreparedStatement stmtUpdate = null;
        PreparedStatement stmtTx = null;
        ResultSet rsCuenta = null;
        ResultSet rsTx = null;

        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción ACID

            // 1. Validar y bloquear la cuenta
            String sqlCuenta = "SELECT id_cuenta, numero_cuenta, saldo, estado FROM cuentas WHERE id_cuenta = ? FOR UPDATE";
            stmtCuenta = conn.prepareStatement(sqlCuenta);
            stmtCuenta.setInt(1, idCuenta);
            rsCuenta = stmtCuenta.executeQuery();

            if (!rsCuenta.next()) {
                throw new CuentaNoDisponibleException("La cuenta bancaria con ID " + idCuenta + " no existe.");
            }

            String numeroCuenta = rsCuenta.getString("numero_cuenta");
            double saldoActual = rsCuenta.getDouble("saldo");
            EstadoCuenta estado = EstadoCuenta.desdeString(rsCuenta.getString("estado"));

            if (estado != EstadoCuenta.ACTIVA) {
                throw new CuentaBloqueadaException("La cuenta " + numeroCuenta + " está en estado " + estado +
                        " y no permite retiros.");
            }

            if (saldoActual < monto) {
                throw new FondosInsuficientesException(saldoActual, monto);
            }

            double saldoResultante = saldoActual - monto;

            // 2. Actualizar saldo
            String sqlUpdate = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";
            stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setDouble(1, saldoResultante);
            stmtUpdate.setInt(2, idCuenta);
            stmtUpdate.executeUpdate();

            // 3. Registrar transacción
            String codigoTx = generarCodigo("RET");
            String descFinal = (descripcion != null && !descripcion.trim().isEmpty())
                    ? descripcion.trim()
                    : "Retiro en ventanilla de cuenta " + numeroCuenta;

            String sqlTx = "INSERT INTO transacciones (codigo_transaccion, id_cuenta_origen, id_cuenta_destino, " +
                           "tipo_transaccion, monto, saldo_resultante, descripcion) " +
                           "VALUES (?, ?, NULL, 'RETIRO', ?, ?, ?)";
            stmtTx = conn.prepareStatement(sqlTx, Statement.RETURN_GENERATED_KEYS);
            stmtTx.setString(1, codigoTx);
            stmtTx.setInt(2, idCuenta);
            stmtTx.setDouble(3, monto);
            stmtTx.setDouble(4, saldoResultante);
            stmtTx.setString(5, descFinal);
            stmtTx.executeUpdate();

            rsTx = stmtTx.getGeneratedKeys();
            int idTx = 0;
            if (rsTx.next()) {
                idTx = rsTx.getInt(1);
            }

            conn.commit();

            Transaccion tx = new Transaccion(idTx, codigoTx, idCuenta, null, Transaccion.Tipo.RETIRO,
                    monto, saldoResultante, descFinal, new java.sql.Timestamp(System.currentTimeMillis()));
            tx.setNumeroCuentaOrigen(numeroCuenta);
            return tx;

        } catch (SQLException e) {
            revertir(conn);
            throw new BancoException("Error en base de datos al realizar retiro: " + e.getMessage(), e);
        } catch (BancoException e) {
            revertir(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
            ConexionBD.cerrar(rsCuenta);
            ConexionBD.cerrar(stmtCuenta);
            ConexionBD.cerrar(stmtUpdate);
            ConexionBD.cerrar(rsTx);
            ConexionBD.cerrar(stmtTx);
            ConexionBD.cerrar(conn);
        }
    }

    /**
     * Realiza una transferencia atómica entre dos cuentas distintas con garantía ACID total.
     */
    public Transaccion realizarTransferencia(int idCuentaOrigen, int idCuentaDestino, double monto, String descripcion)
            throws BancoException {
        if (idCuentaOrigen == idCuentaDestino) {
            throw new TransaccionInvalidaException("La cuenta de origen y de destino no pueden ser iguales.");
        }
        if (monto <= 0) {
            throw new TransaccionInvalidaException("El monto a transferir debe ser mayor a $0.00.");
        }

        Connection conn = null;
        PreparedStatement stmtOrigen = null;
        PreparedStatement stmtUpdOrigen = null;
        PreparedStatement stmtUpdDestino = null;
        PreparedStatement stmtTx = null;
        ResultSet rsOrigen = null;
        ResultSet rsTx = null;

        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción ACID

            // 1. Bloquear AMBAS cuentas en una sola sentencia y en orden ascendente de ID.
            //    Un orden de bloqueo fijo evita el deadlock entre transferencias cruzadas
            //    (A→B y B→A simultáneas), que antes bloqueaban origen y luego destino.
            String sqlBloqueo = "SELECT id_cuenta, numero_cuenta, saldo, estado FROM cuentas " +
                                "WHERE id_cuenta IN (?, ?) ORDER BY id_cuenta ASC FOR UPDATE";
            stmtOrigen = conn.prepareStatement(sqlBloqueo);
            stmtOrigen.setInt(1, idCuentaOrigen);
            stmtOrigen.setInt(2, idCuentaDestino);
            rsOrigen = stmtOrigen.executeQuery();

            String numOrigen = null;
            double saldoOrigen = 0;
            EstadoCuenta estOrigen = null;
            String numDestino = null;
            double saldoDestino = 0;
            EstadoCuenta estDestino = null;

            while (rsOrigen.next()) {
                int idFila = rsOrigen.getInt("id_cuenta");
                if (idFila == idCuentaOrigen) {
                    numOrigen = rsOrigen.getString("numero_cuenta");
                    saldoOrigen = rsOrigen.getDouble("saldo");
                    estOrigen = EstadoCuenta.desdeString(rsOrigen.getString("estado"));
                } else if (idFila == idCuentaDestino) {
                    numDestino = rsOrigen.getString("numero_cuenta");
                    saldoDestino = rsOrigen.getDouble("saldo");
                    estDestino = EstadoCuenta.desdeString(rsOrigen.getString("estado"));
                }
            }

            // 2. Validaciones de negocio (mismo orden de precedencia de errores que antes)
            if (estOrigen == null) {
                throw new CuentaNoDisponibleException("La cuenta de origen con ID " + idCuentaOrigen + " no existe.");
            }

            if (estOrigen != EstadoCuenta.ACTIVA) {
                throw new CuentaBloqueadaException("La cuenta de origen " + numOrigen + " está en estado " +
                        estOrigen + " y no puede emitir transferencias.");
            }

            if (saldoOrigen < monto) {
                throw new FondosInsuficientesException(saldoOrigen, monto);
            }

            if (estDestino == null) {
                throw new CuentaNoDisponibleException("La cuenta de destino con ID " + idCuentaDestino + " no existe.");
            }

            if (estDestino != EstadoCuenta.ACTIVA) {
                throw new CuentaBloqueadaException("La cuenta de destino " + numDestino + " está en estado " +
                        estDestino + " y no puede recibir transferencias.");
            }

            double saldoResultanteOrigen = saldoOrigen - monto;
            double saldoResultanteDestino = saldoDestino + monto;

            // 3. Descontar de cuenta origen
            String sqlUpdOrigen = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";
            stmtUpdOrigen = conn.prepareStatement(sqlUpdOrigen);
            stmtUpdOrigen.setDouble(1, saldoResultanteOrigen);
            stmtUpdOrigen.setInt(2, idCuentaOrigen);
            stmtUpdOrigen.executeUpdate();

            // 4. Acreditar en cuenta destino
            String sqlUpdDestino = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";
            stmtUpdDestino = conn.prepareStatement(sqlUpdDestino);
            stmtUpdDestino.setDouble(1, saldoResultanteDestino);
            stmtUpdDestino.setInt(2, idCuentaDestino);
            stmtUpdDestino.executeUpdate();

            // 5. Registrar transacción
            String codigoTx = generarCodigo("TRF");
            String descFinal = (descripcion != null && !descripcion.trim().isEmpty())
                    ? descripcion.trim()
                    : "Transferencia de " + numOrigen + " hacia " + numDestino;

            String sqlTx = "INSERT INTO transacciones (codigo_transaccion, id_cuenta_origen, id_cuenta_destino, " +
                           "tipo_transaccion, monto, saldo_resultante, descripcion) " +
                           "VALUES (?, ?, ?, 'TRANSFERENCIA', ?, ?, ?)";
            stmtTx = conn.prepareStatement(sqlTx, Statement.RETURN_GENERATED_KEYS);
            stmtTx.setString(1, codigoTx);
            stmtTx.setInt(2, idCuentaOrigen);
            stmtTx.setInt(3, idCuentaDestino);
            stmtTx.setDouble(4, monto);
            stmtTx.setDouble(5, saldoResultanteOrigen);
            stmtTx.setString(6, descFinal);
            stmtTx.executeUpdate();

            rsTx = stmtTx.getGeneratedKeys();
            int idTx = 0;
            if (rsTx.next()) {
                idTx = rsTx.getInt(1);
            }

            conn.commit(); // Éxito atómico

            Transaccion tx = new Transaccion(idTx, codigoTx, idCuentaOrigen, idCuentaDestino,
                    Transaccion.Tipo.TRANSFERENCIA, monto, saldoResultanteOrigen, descFinal,
                    new java.sql.Timestamp(System.currentTimeMillis()));
            tx.setNumeroCuentaOrigen(numOrigen);
            tx.setNumeroCuentaDestino(numDestino);
            return tx;

        } catch (SQLException e) {
            revertir(conn);
            throw new BancoException("Error en base de datos al procesar transferencia: " + e.getMessage(), e);
        } catch (BancoException e) {
            revertir(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
            ConexionBD.cerrar(rsOrigen);
            ConexionBD.cerrar(stmtOrigen);
            ConexionBD.cerrar(stmtUpdOrigen);
            ConexionBD.cerrar(stmtUpdDestino);
            ConexionBD.cerrar(rsTx);
            ConexionBD.cerrar(stmtTx);
            ConexionBD.cerrar(conn);
        }
    }

    /**
     * Consulta un movimiento por su ID primario.
     */
    public Transaccion buscarPorId(int idTransaccion) throws BancoException {
        String sql = "SELECT t.*, " +
                     "co.numero_cuenta AS num_origen, clo.nombre_completo AS titular_origen, " +
                     "cd.numero_cuenta AS num_destino, cld.nombre_completo AS titular_destino " +
                     "FROM transacciones t " +
                     "LEFT JOIN cuentas co ON t.id_cuenta_origen = co.id_cuenta " +
                     "LEFT JOIN clientes clo ON co.id_cliente = clo.id_cliente " +
                     "LEFT JOIN cuentas cd ON t.id_cuenta_destino = cd.id_cuenta " +
                     "LEFT JOIN clientes cld ON cd.id_cliente = cld.id_cliente " +
                     "WHERE t.id_transaccion = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idTransaccion);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearTransaccion(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar transacción por ID: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    /**
     * Consulta un movimiento por su código de transacción único.
     */
    public Transaccion buscarPorCodigo(String codigo) throws BancoException {
        String sql = "SELECT t.*, " +
                     "co.numero_cuenta AS num_origen, clo.nombre_completo AS titular_origen, " +
                     "cd.numero_cuenta AS num_destino, cld.nombre_completo AS titular_destino " +
                     "FROM transacciones t " +
                     "LEFT JOIN cuentas co ON t.id_cuenta_origen = co.id_cuenta " +
                     "LEFT JOIN clientes clo ON co.id_cliente = clo.id_cliente " +
                     "LEFT JOIN cuentas cd ON t.id_cuenta_destino = cd.id_cuenta " +
                     "LEFT JOIN clientes cld ON cd.id_cliente = cld.id_cliente " +
                     "WHERE UPPER(t.codigo_transaccion) = UPPER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, codigo.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearTransaccion(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BancoException("Error al buscar transacción por código: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    /**
     * Obtiene el listado de todas las transacciones ordenadas de la más reciente a la más antigua.
     */
    public List<Transaccion> listarTodas() throws BancoException {
        String sql = "SELECT t.*, " +
                     "co.numero_cuenta AS num_origen, clo.nombre_completo AS titular_origen, " +
                     "cd.numero_cuenta AS num_destino, cld.nombre_completo AS titular_destino " +
                     "FROM transacciones t " +
                     "LEFT JOIN cuentas co ON t.id_cuenta_origen = co.id_cuenta " +
                     "LEFT JOIN clientes clo ON co.id_cliente = clo.id_cliente " +
                     "LEFT JOIN cuentas cd ON t.id_cuenta_destino = cd.id_cuenta " +
                     "LEFT JOIN clientes cld ON cd.id_cliente = cld.id_cliente " +
                     "ORDER BY t.id_transaccion DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Transaccion> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearTransaccion(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al listar transacciones: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    /**
     * Obtiene el historial de movimientos de una cuenta específica (como origen o destino).
     */
    public List<Transaccion> listarPorCuenta(int idCuenta) throws BancoException {
        String sql = "SELECT t.*, " +
                     "co.numero_cuenta AS num_origen, clo.nombre_completo AS titular_origen, " +
                     "cd.numero_cuenta AS num_destino, cld.nombre_completo AS titular_destino " +
                     "FROM transacciones t " +
                     "LEFT JOIN cuentas co ON t.id_cuenta_origen = co.id_cuenta " +
                     "LEFT JOIN clientes clo ON co.id_cliente = clo.id_cliente " +
                     "LEFT JOIN cuentas cd ON t.id_cuenta_destino = cd.id_cuenta " +
                     "LEFT JOIN clientes cld ON cd.id_cliente = cld.id_cliente " +
                     "WHERE t.id_cuenta_origen = ? OR t.id_cuenta_destino = ? " +
                     "ORDER BY t.id_transaccion DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Transaccion> lista = new ArrayList<>();
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCuenta);
            stmt.setInt(2, idCuenta);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearTransaccion(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new BancoException("Error al listar transacciones por cuenta: " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    // =========================================================================
    // MÉTODOS DE REPORTES Y CONSOLIDADOS CONTABLES (Fase 2)
    // =========================================================================

    public double obtenerTotalDepositado() throws BancoException {
        return obtenerSumaPorTipo("DEPOSITO");
    }

    public double obtenerTotalRetirado() throws BancoException {
        return obtenerSumaPorTipo("RETIRO");
    }

    public double obtenerTotalTransferido() throws BancoException {
        return obtenerSumaPorTipo("TRANSFERENCIA");
    }

    private double obtenerSumaPorTipo(String tipo) throws BancoException {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM transacciones WHERE tipo_transaccion = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.obtenerConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tipo);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new BancoException("Error al calcular total de " + tipo + ": " + e.getMessage(), e);
        } finally {
            ConexionBD.cerrar(conn, stmt, rs);
        }
    }

    private Transaccion mapearTransaccion(ResultSet rs) throws SQLException {
        Transaccion t = new Transaccion();
        t.setIdTransaccion(rs.getInt("id_transaccion"));
        t.setCodigoTransaccion(rs.getString("codigo_transaccion"));
        t.setIdCuentaOrigen(rs.getInt("id_cuenta_origen"));

        int idDest = rs.getInt("id_cuenta_destino");
        if (rs.wasNull()) {
            t.setIdCuentaDestino(null);
        } else {
            t.setIdCuentaDestino(idDest);
        }

        t.setTipo(Transaccion.Tipo.desdeString(rs.getString("tipo_transaccion")));
        t.setMonto(rs.getDouble("monto"));
        t.setSaldoResultante(rs.getDouble("saldo_resultante"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setFechaHora(rs.getTimestamp("fecha_hora"));

        // Datos enriquecidos
        t.setNumeroCuentaOrigen(rs.getString("num_origen"));
        t.setNumeroCuentaDestino(rs.getString("num_destino"));
        t.setTitularOrigen(rs.getString("titular_origen"));
        t.setTitularDestino(rs.getString("titular_destino"));

        return t;
    }
}

