package banco.servicio;

import banco.datos.ClienteDAO;
import banco.datos.CuentaDAO;
import banco.excepciones.BancoException;
import banco.excepciones.ClienteNoEncontradoException;
import banco.excepciones.CuentaNoDisponibleException;
import banco.excepciones.DatoInvalidoException;
import banco.excepciones.SaldoInvalidoException;
import banco.modelo.Cliente;
import banco.modelo.Cuenta;
import banco.modelo.EstadoCliente;
import banco.modelo.EstadoCuenta;
import banco.util.Validador;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Capa de Servicios del Banco Académico UDB.
 * Contiene la lógica de negocio y validaciones de dominio.
 * Diseñada para ser 100% reutilizable por la UI Swing (Fase 1 y 2) y por los Servlets Web (Fase 3).
 */
public class BancoServicio {

    private final ClienteDAO clienteDAO;
    private final CuentaDAO cuentaDAO;

    public BancoServicio() {
        this.clienteDAO = new ClienteDAO();
        this.cuentaDAO = new CuentaDAO();
    }

    public BancoServicio(ClienteDAO clienteDAO, CuentaDAO cuentaDAO) {
        this.clienteDAO = clienteDAO;
        this.cuentaDAO = cuentaDAO;
    }

    // =========================================================================
    // SECCIÓN 1: GESTIÓN DE CLIENTES
    // =========================================================================

    public int registrarCliente(Cliente cliente) throws BancoException {
        if (cliente == null) {
            throw new DatoInvalidoException("El cliente no puede ser nulo.");
        }
        Validador.validarCodigoCliente(cliente.getCodigoCliente());
        Validador.validarDui(cliente.getDui());
        Validador.validarTextoNoVacio(cliente.getNombreCompleto(), "Nombre Completo");
        Validador.validarTelefono(cliente.getTelefono());
        Validador.validarCorreo(cliente.getCorreoElectronico());
        Validador.validarTextoNoVacio(cliente.getDireccion(), "Dirección");

        if (clienteDAO.buscarPorDui(cliente.getDui()) != null) {
            throw new DatoInvalidoException("Ya existe un cliente registrado con el DUI: " + cliente.getDui());
        }
        if (clienteDAO.buscarPorCodigo(cliente.getCodigoCliente()) != null) {
            throw new DatoInvalidoException("Ya existe un cliente registrado con el código: " + cliente.getCodigoCliente());
        }

        return clienteDAO.insertar(cliente);
    }

    public boolean actualizarCliente(Cliente cliente) throws BancoException {
        if (cliente == null || cliente.getId() <= 0) {
            throw new DatoInvalidoException("Identificador de cliente inválido para actualización.");
        }
        Validador.validarCodigoCliente(cliente.getCodigoCliente());
        Validador.validarDui(cliente.getDui());
        Validador.validarTextoNoVacio(cliente.getNombreCompleto(), "Nombre Completo");
        Validador.validarTelefono(cliente.getTelefono());
        Validador.validarCorreo(cliente.getCorreoElectronico());
        Validador.validarTextoNoVacio(cliente.getDireccion(), "Dirección");

        Cliente existente = clienteDAO.buscarPorId(cliente.getId());
        if (existente == null) {
            throw new ClienteNoEncontradoException("No se encontró el cliente a actualizar con ID: " + cliente.getId());
        }

        return clienteDAO.actualizar(cliente);
    }

    public boolean eliminarCliente(int idCliente) throws BancoException {
        if (idCliente <= 0) {
            throw new DatoInvalidoException("ID de cliente inválido.");
        }
        Cliente existente = clienteDAO.buscarPorId(idCliente);
        if (existente == null) {
            throw new ClienteNoEncontradoException("El cliente no existe o ya fue eliminado.");
        }
        if (clienteDAO.tieneCuentasAsociadas(idCliente)) {
            throw new BancoException("No se puede eliminar el cliente '" + existente.getNombreCompleto() +
                    "' porque posee cuentas bancarias asociadas.");
        }
        return clienteDAO.eliminar(idCliente);
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
            throw new DatoInvalidoException("La cuenta bancaria no puede ser nula.");
        }
        Validador.validarNumeroCuenta(cuenta.getNumeroCuenta());
        Validador.validarSaldoInicial(cuenta.getSaldo());

        Cliente cliente = clienteDAO.buscarPorId(cuenta.getIdCliente());
        if (cliente == null) {
            throw new ClienteNoEncontradoException("No se puede aperturar la cuenta: El cliente con ID " +
                    cuenta.getIdCliente() + " no existe.");
        }
        if (!cliente.isActivo()) {
            throw new CuentaNoDisponibleException("No se permite abrir cuentas a clientes en estado " +
                    cliente.getEstado() + ". El cliente debe estar ACTIVO.");
        }

        if (cuentaDAO.buscarPorNumero(cuenta.getNumeroCuenta()) != null) {
            throw new DatoInvalidoException("El número de cuenta '" + cuenta.getNumeroCuenta() + "' ya está registrado.");
        }

        return cuentaDAO.insertar(cuenta);
    }

    public boolean cambiarEstadoCuenta(int idCuenta, EstadoCuenta nuevoEstado) throws BancoException {
        if (idCuenta <= 0) {
            throw new DatoInvalidoException("ID de cuenta inválido.");
        }
        Cuenta cuenta = cuentaDAO.buscarPorId(idCuenta);
        if (cuenta == null) {
            throw new CuentaNoDisponibleException("La cuenta bancaria con ID " + idCuenta + " no existe.");
        }
        return cuentaDAO.actualizarEstado(idCuenta, nuevoEstado);
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
    // SECCIÓN 3: ESTADÍSTICAS DEL SISTEMA (Dashboard & Reportes)
    // =========================================================================

    public Map<String, Object> obtenerEstadisticasGlobales() throws BancoException {
        Map<String, Object> stats = new HashMap<>();
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Cuenta> cuentas = cuentaDAO.listarTodas();

        long clientesActivos = clientes.stream().filter(Cliente::isActivo).count();
        long clientesBloqueados = clientes.stream().filter(Cliente::isBloqueado).count();
        long cuentasActivas = cuentas.stream().filter(c -> c.getEstado() == EstadoCuenta.ACTIVA).count();
        long cuentasBloqueadas = cuentas.stream().filter(c -> c.getEstado() == EstadoCuenta.BLOQUEADA).count();
        double saldoTotal = cuentaDAO.obtenerTotalSaldosGlobal();

        stats.put("totalClientes", clientes.size());
        stats.put("clientesActivos", clientesActivos);
        stats.put("clientesBloqueados", clientesBloqueados);
        stats.put("totalCuentas", cuentas.size());
        stats.put("cuentasActivas", cuentasActivas);
        stats.put("cuentasBloqueadas", cuentasBloqueadas);
        stats.put("saldoTotalCustodia", saldoTotal);

        return stats;
    }
}
