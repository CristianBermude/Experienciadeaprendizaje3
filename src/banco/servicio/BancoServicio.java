package banco.servicio;

import banco.datos.ClienteDAO;
import banco.datos.CuentaDAO;
import banco.datos.TransaccionDAO;
import banco.excepciones.BancoException;
import banco.excepciones.ClienteNoEncontradoException;
import banco.excepciones.CuentaNoDisponibleException;
import banco.excepciones.DatoInvalidoException;
import banco.modelo.Cliente;
import banco.modelo.Cuenta;
import banco.modelo.EstadoCuenta;
import banco.modelo.Transaccion;
import banco.util.Validador;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Capa de Servicios del Banco Académico UDB.
 * Contiene la lógica de negocio, validaciones de dominio, transacciones financieras
 * y generación de reportes con bitácora centralizada mediante Log4J.
 */
public class BancoServicio {

    private static final Logger logger = LogManager.getLogger(BancoServicio.class);

    private final ClienteDAO clienteDAO;
    private final CuentaDAO cuentaDAO;
    private final TransaccionDAO transaccionDAO;

    public BancoServicio() {
        this.clienteDAO = new ClienteDAO();
        this.cuentaDAO = new CuentaDAO();
        this.transaccionDAO = new TransaccionDAO();
        logger.info("Servicio bancario inicializado correctamente.");
    }

    public BancoServicio(ClienteDAO clienteDAO, CuentaDAO cuentaDAO, TransaccionDAO transaccionDAO) {
        this.clienteDAO = clienteDAO;
        this.cuentaDAO = cuentaDAO;
        this.transaccionDAO = transaccionDAO;
        logger.info("Servicio bancario inicializado con DAOs inyectados.");
    }

    // =========================================================================
    // SECCIÓN 1: GESTIÓN DE CLIENTES
    // =========================================================================

    public int registrarCliente(Cliente cliente) throws BancoException {
        if (cliente == null) {
            logger.warn("Intento de registrar cliente nulo.");
            throw new DatoInvalidoException("El cliente no puede ser nulo.");
        }
        try {
            Validador.validarCodigoCliente(cliente.getCodigoCliente());
            Validador.validarDui(cliente.getDui());
            Validador.validarTextoNoVacio(cliente.getNombreCompleto(), "Nombre Completo");
            Validador.validarTelefono(cliente.getTelefono());
            Validador.validarCorreo(cliente.getCorreoElectronico());
            Validador.validarTextoNoVacio(cliente.getDireccion(), "Dirección");

            if (clienteDAO.buscarPorDui(cliente.getDui()) != null) {
                logger.warn("Intento de registro con DUI duplicado: {}", cliente.getDui());
                throw new DatoInvalidoException("Ya existe un cliente registrado con el DUI: " + cliente.getDui());
            }
            if (clienteDAO.buscarPorCodigo(cliente.getCodigoCliente()) != null) {
                logger.warn("Intento de registro con Código duplicado: {}", cliente.getCodigoCliente());
                throw new DatoInvalidoException("Ya existe un cliente registrado con el código: " + cliente.getCodigoCliente());
            }

            int idGenerado = clienteDAO.insertar(cliente);
            logger.info("Cliente registrado exitosamente [ID={}, Código={}, DUI={}, Nombre={}]",
                    idGenerado, cliente.getCodigoCliente(), cliente.getDui(), cliente.getNombreCompleto());
            return idGenerado;

        } catch (BancoException e) {
            logger.error("Error al registrar cliente: {}", e.getMessage());
            throw e;
        }
    }

    public boolean actualizarCliente(Cliente cliente) throws BancoException {
        if (cliente == null || cliente.getId() <= 0) {
            logger.warn("Intento de actualizar cliente con ID inválido.");
            throw new DatoInvalidoException("Identificador de cliente inválido para actualización.");
        }
        try {
            Validador.validarCodigoCliente(cliente.getCodigoCliente());
            Validador.validarDui(cliente.getDui());
            Validador.validarTextoNoVacio(cliente.getNombreCompleto(), "Nombre Completo");
            Validador.validarTelefono(cliente.getTelefono());
            Validador.validarCorreo(cliente.getCorreoElectronico());
            Validador.validarTextoNoVacio(cliente.getDireccion(), "Dirección");

            Cliente existente = clienteDAO.buscarPorId(cliente.getId());
            if (existente == null) {
                logger.warn("Cliente no encontrado para actualización [ID={}]", cliente.getId());
                throw new ClienteNoEncontradoException("No se encontró el cliente a actualizar con ID: " + cliente.getId());
            }

            boolean ok = clienteDAO.actualizar(cliente);
            if (ok) {
                logger.info("Cliente actualizado exitosamente [ID={}, Nombre={}, Estado={}]",
                        cliente.getId(), cliente.getNombreCompleto(), cliente.getEstado());
            }
            return ok;

        } catch (BancoException e) {
            logger.error("Error al actualizar cliente [ID={}]: {}", cliente.getId(), e.getMessage());
            throw e;
        }
    }

    public boolean eliminarCliente(int idCliente) throws BancoException {
        if (idCliente <= 0) {
            throw new DatoInvalidoException("ID de cliente inválido.");
        }
        try {
            Cliente existente = clienteDAO.buscarPorId(idCliente);
            if (existente == null) {
                logger.warn("Intento de eliminar cliente inexistente [ID={}]", idCliente);
                throw new ClienteNoEncontradoException("El cliente no existe o ya fue eliminado.");
            }
            if (clienteDAO.tieneCuentasAsociadas(idCliente)) {
                logger.warn("Bloqueo de eliminación: Cliente [ID={}, Nombre={}] posee cuentas asociadas.",
                        idCliente, existente.getNombreCompleto());
                throw new BancoException("No se puede eliminar el cliente '" + existente.getNombreCompleto() +
                        "' porque posee cuentas bancarias asociadas.");
            }

            boolean ok = clienteDAO.eliminar(idCliente);
            if (ok) {
                logger.info("Cliente eliminado exitosamente [ID={}, Nombre={}]", idCliente, existente.getNombreCompleto());
            }
            return ok;

        } catch (BancoException e) {
            logger.error("Error al eliminar cliente [ID={}]: {}", idCliente, e.getMessage());
            throw e;
        }
    }

    public Cliente buscarClientePorId(int idCliente) throws BancoException {
        return clienteDAO.buscarPorId(idCliente);
    }

    public Cliente buscarClientePorDui(String dui) throws BancoException {
        return clienteDAO.buscarPorDui(dui);
    }

    public Cliente buscarClientePorCodigo(String codigo) throws BancoException {
        return clienteDAO.buscarPorCodigo(codigo);
    }

    public List<Cliente> listarClientes() throws BancoException {
        return clienteDAO.listarTodos();
    }

    public List<Cliente> buscarClientes(String termino) throws BancoException {
        if (termino == null || termino.trim().isEmpty()) {
            return listarClientes();
        }
        return clienteDAO.buscarPorTermino(termino);
    }

    // =========================================================================
    // SECCIÓN 2: GESTIÓN DE CUENTAS BANCARIAS
    // =========================================================================

    public int aperturarCuenta(Cuenta cuenta) throws BancoException {
        if (cuenta == null) {
            logger.warn("Intento de apertura con cuenta nula.");
            throw new DatoInvalidoException("La cuenta bancaria no puede ser nula.");
        }
        try {
            Validador.validarNumeroCuenta(cuenta.getNumeroCuenta());
            Validador.validarSaldoInicial(cuenta.getSaldo());

            Cliente cliente = clienteDAO.buscarPorId(cuenta.getIdCliente());
            if (cliente == null) {
                logger.warn("Apertura rechazada: Cliente titular [ID={}] no existe.", cuenta.getIdCliente());
                throw new ClienteNoEncontradoException("No se puede aperturar la cuenta: El cliente con ID " +
                        cuenta.getIdCliente() + " no existe.");
            }
            if (!cliente.isActivo()) {
                logger.warn("Apertura rechazada: Cliente titular [ID={}, Estado={}] no está ACTIVO.",
                        cliente.getId(), cliente.getEstado());
                throw new CuentaNoDisponibleException("No se permite abrir cuentas a clientes en estado " +
                        cliente.getEstado() + ". El cliente debe estar ACTIVO.");
            }

            if (cuentaDAO.buscarPorNumero(cuenta.getNumeroCuenta()) != null) {
                logger.warn("Apertura rechazada: Número de cuenta duplicado '{}'", cuenta.getNumeroCuenta());
                throw new DatoInvalidoException("El número de cuenta '" + cuenta.getNumeroCuenta() + "' ya está registrado.");
            }

            int idGenerado = cuentaDAO.insertar(cuenta);
            logger.info("Cuenta aperturada exitosamente [ID={}, Número={}, Tipo={}, Titular={}, SaldoInicial=${}]",
                    idGenerado, cuenta.getNumeroCuenta(), cuenta.getTipoCuenta(), cliente.getNombreCompleto(), cuenta.getSaldo());
            return idGenerado;

        } catch (BancoException e) {
            logger.error("Error al aperturar cuenta: {}", e.getMessage());
            throw e;
        }
    }

    public boolean cambiarEstadoCuenta(int idCuenta, EstadoCuenta nuevoEstado) throws BancoException {
        if (idCuenta <= 0) {
            throw new DatoInvalidoException("ID de cuenta inválido.");
        }
        try {
            Cuenta cuenta = cuentaDAO.buscarPorId(idCuenta);
            if (cuenta == null) {
                logger.warn("Cambio de estado rechazado: Cuenta [ID={}] no existe.", idCuenta);
                throw new CuentaNoDisponibleException("La cuenta bancaria con ID " + idCuenta + " no existe.");
            }
            boolean ok = cuentaDAO.actualizarEstado(idCuenta, nuevoEstado);
            if (ok) {
                logger.info("Estado de cuenta modificado [ID={}, Número={}, EstadoAnterior={}, NuevoEstado={}]",
                        idCuenta, cuenta.getNumeroCuenta(), cuenta.getEstado(), nuevoEstado);
            }
            return ok;
        } catch (BancoException e) {
            logger.error("Error al cambiar estado de cuenta [ID={}]: {}", idCuenta, e.getMessage());
            throw e;
        }
    }

    public Cuenta buscarCuentaPorId(int idCuenta) throws BancoException {
        return cuentaDAO.buscarPorId(idCuenta);
    }

    public Cuenta buscarCuentaPorNumero(String numeroCuenta) throws BancoException {
        return cuentaDAO.buscarPorNumero(numeroCuenta);
    }

    public List<Cuenta> listarCuentas() throws BancoException {
        return cuentaDAO.listarTodas();
    }

    public List<Cuenta> listarCuentasDeCliente(int idCliente) throws BancoException {
        return cuentaDAO.listarPorCliente(idCliente);
    }

    // =========================================================================
    // SECCIÓN 3: GESTIÓN DE TRANSACCIONES (Fase 2)
    // =========================================================================

    public Transaccion realizarDeposito(int idCuenta, double monto, String descripcion) throws BancoException {
        try {
            logger.info("Iniciando solicitud de depósito [Cuenta ID={}, Monto=${}]", idCuenta, monto);
            Transaccion tx = transaccionDAO.realizarDeposito(idCuenta, monto, descripcion);
            logger.info("DEPÓSITO EXITOSO [Código={}, Cuenta={}, Monto=${}, SaldoNuevo=${}]",
                    tx.getCodigoTransaccion(), tx.getNumeroCuentaOrigen(), tx.getMonto(), tx.getSaldoResultante());
            return tx;
        } catch (BancoException e) {
            logger.error("DEPÓSITO RECHAZADO [Cuenta ID={}, Monto=${}]: {}", idCuenta, monto, e.getMessage());
            throw e;
        }
    }

    public Transaccion realizarRetiro(int idCuenta, double monto, String descripcion) throws BancoException {
        try {
            logger.info("Iniciando solicitud de retiro [Cuenta ID={}, Monto=${}]", idCuenta, monto);
            Transaccion tx = transaccionDAO.realizarRetiro(idCuenta, monto, descripcion);
            logger.info("RETIRO EXITOSO [Código={}, Cuenta={}, Monto=${}, SaldoNuevo=${}]",
                    tx.getCodigoTransaccion(), tx.getNumeroCuentaOrigen(), tx.getMonto(), tx.getSaldoResultante());
            return tx;
        } catch (BancoException e) {
            logger.error("RETIRO RECHAZADO [Cuenta ID={}, Monto=${}]: {}", idCuenta, monto, e.getMessage());
            throw e;
        }
    }

    public Transaccion realizarTransferencia(int idCuentaOrigen, int idCuentaDestino, double monto, String descripcion)
            throws BancoException {
        try {
            logger.info("Iniciando solicitud de transferencia [Origen ID={}, Destino ID={}, Monto=${}]",
                    idCuentaOrigen, idCuentaDestino, monto);
            Transaccion tx = transaccionDAO.realizarTransferencia(idCuentaOrigen, idCuentaDestino, monto, descripcion);
            logger.info("TRANSFERENCIA EXITOSA [Código={}, Origen={}, Destino={}, Monto=${}, SaldoResultanteOrigen=${}]",
                    tx.getCodigoTransaccion(), tx.getNumeroCuentaOrigen(), tx.getNumeroCuentaDestino(),
                    tx.getMonto(), tx.getSaldoResultante());
            return tx;
        } catch (BancoException e) {
            logger.error("TRANSFERENCIA RECHAZADA [Origen ID={}, Destino ID={}, Monto=${}]: {}",
                    idCuentaOrigen, idCuentaDestino, monto, e.getMessage());
            throw e;
        }
    }

    public Transaccion buscarTransaccionPorId(int idTransaccion) throws BancoException {
        return transaccionDAO.buscarPorId(idTransaccion);
    }

    public Transaccion buscarTransaccionPorCodigo(String codigo) throws BancoException {
        return transaccionDAO.buscarPorCodigo(codigo);
    }

    public List<Transaccion> listarTransacciones() throws BancoException {
        return transaccionDAO.listarTodas();
    }

    public List<Transaccion> listarTransaccionesPorCuenta(int idCuenta) throws BancoException {
        return transaccionDAO.listarPorCuenta(idCuenta);
    }

    // =========================================================================
    // SECCIÓN 4: GESTIÓN DE REPORTES DEL SISTEMA (Fase 2)
    // =========================================================================

    /**
     * Reporte 1: Cuentas bancarias activas.
     */
    public List<Cuenta> reporteCuentasActivas() throws BancoException {
        logger.debug("Generando reporte de Cuentas Activas...");
        return cuentaDAO.listarPorEstado(EstadoCuenta.ACTIVA);
    }

    /**
     * Reporte 2: Cuentas bancarias bloqueadas.
     */
    public List<Cuenta> reporteCuentasBloqueadas() throws BancoException {
        logger.debug("Generando reporte de Cuentas Bloqueadas...");
        return cuentaDAO.listarPorEstado(EstadoCuenta.BLOQUEADA);
    }

    /**
     * Reporte 3: Clientes registrados con consolidado de cuentas y saldo global.
     */
    public List<Map<String, Object>> reporteClientesRegistrados() throws BancoException {
        logger.debug("Generando reporte consolidado de Clientes Registrados...");
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Cliente c : clientes) {
            List<Cuenta> cuentas = cuentaDAO.listarPorCliente(c.getId());
            double saldoTotalCliente = 0.0;
            for (Cuenta cta : cuentas) {
                saldoTotalCliente += cta.getSaldo();
            }

            Map<String, Object> fila = new HashMap<>();
            fila.put("cliente", c);
            fila.put("cantidadCuentas", cuentas.size());
            fila.put("saldoTotal", saldoTotalCliente);
            filas.add(fila);
        }
        return filas;
    }

    /**
     * Reporte 4: Historial general de transacciones.
     */
    public List<Transaccion> reporteHistorialTransacciones() throws BancoException {
        logger.debug("Generando reporte de Historial General de Transacciones...");
        return transaccionDAO.listarTodas();
    }

    /**
     * Reporte 5: Transacciones por cuenta específica.
     */
    public List<Transaccion> reporteTransaccionesPorCuenta(int idCuenta) throws BancoException {
        logger.debug("Generando reporte de Transacciones para cuenta ID={}", idCuenta);
        return transaccionDAO.listarPorCuenta(idCuenta);
    }

    public double obtenerTotalDepositado() throws BancoException {
        return transaccionDAO.obtenerTotalDepositado();
    }

    public double obtenerTotalRetirado() throws BancoException {
        return transaccionDAO.obtenerTotalRetirado();
    }

    public double obtenerTotalTransferido() throws BancoException {
        return transaccionDAO.obtenerTotalTransferido();
    }

    /**
     * Estadísticas globales completas para el Dashboard y Reportes Gerenciales.
     */
    public Map<String, Object> obtenerEstadisticasGlobales() throws BancoException {
        Map<String, Object> stats = new HashMap<>();
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Cuenta> cuentas = cuentaDAO.listarTodas();

        long clientesActivos = clientes.stream().filter(Cliente::isActivo).count();
        long clientesBloqueados = clientes.stream().filter(Cliente::isBloqueado).count();
        long cuentasActivas = cuentas.stream().filter(c -> c.getEstado() == EstadoCuenta.ACTIVA).count();
        long cuentasBloqueadas = cuentas.stream().filter(c -> c.getEstado() == EstadoCuenta.BLOQUEADA).count();
        double saldoTotal = cuentaDAO.obtenerTotalSaldosGlobal();

        double totalDepositado = transaccionDAO.obtenerTotalDepositado();
        double totalRetirado = transaccionDAO.obtenerTotalRetirado();
        double totalTransferido = transaccionDAO.obtenerTotalTransferido();
        int totalTransacciones = transaccionDAO.listarTodas().size();

        stats.put("totalClientes", clientes.size());
        stats.put("clientesActivos", clientesActivos);
        stats.put("clientesBloqueados", clientesBloqueados);
        stats.put("totalCuentas", cuentas.size());
        stats.put("cuentasActivas", cuentasActivas);
        stats.put("cuentasBloqueadas", cuentasBloqueadas);
        stats.put("saldoTotalCustodia", saldoTotal);

        stats.put("totalDepositado", totalDepositado);
        stats.put("totalRetirado", totalRetirado);
        stats.put("totalTransferido", totalTransferido);
        stats.put("totalTransacciones", totalTransacciones);

        return stats;
    }
}
