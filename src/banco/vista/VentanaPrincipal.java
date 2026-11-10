package banco.vista;

import banco.datos.ConexionBD;
import banco.excepciones.BancoException;
import banco.modelo.Cliente;
import banco.modelo.Cuenta;
import banco.modelo.CuentaAhorro;
import banco.modelo.CuentaCorriente;
import banco.modelo.EstadoCliente;
import banco.modelo.EstadoCuenta;
import banco.modelo.TipoCuenta;
import banco.modelo.Transaccion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import banco.servicio.BancoServicio;
import banco.util.Formateador;
import banco.util.Validador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Ventana Principal del Sistema Bancario Académico UDB.
 * Implementa una interfaz gráfica moderna, ergonómica y profesional en Swing
 * con arquitectura desacoplada en capas (MVC) que integra:
 * - Fase 1: Clientes y Cuentas Bancarias con persistencia JDBC.
 * - Fase 2: Operaciones y Transacciones ACID, Reportes y Bitácora Log4J.
 */
public class VentanaPrincipal extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaPrincipal.class);

    private final BancoServicio servicio;

    // =========================================================================
    // COMPONENTES: DASHBOARD / RESUMEN
    // =========================================================================
    private JLabel lblTotalClientes;
    private JLabel lblTotalCuentas;
    private JLabel lblCuentasActivas;
    private JLabel lblCuentasBloqueadas;
    private JLabel lblSaldoGlobal;
    private JLabel lblDashTotalDepositado;
    private JLabel lblDashTotalRetirado;
    private JLabel lblDashTotalTransferido;
    private JLabel lblDashTotalTx;
    private JLabel lblEstadoBD;

    // =========================================================================
    // COMPONENTES: GESTIÓN DE CLIENTES
    // =========================================================================
    private JTextField txtCliId;
    private JTextField txtCliCodigo;
    private JTextField txtCliDui;
    private JTextField txtCliNombre;
    private JTextField txtCliTelefono;
    private JTextField txtCliCorreo;
    private JTextField txtCliDireccion;
    private JComboBox<EstadoCliente> cmbCliEstado;
    private JTextField txtCliBuscar;
    private JTable tblClientes;
    private DefaultTableModel modeloTablaClientes;
    private JButton btnCliGuardar;
    private JButton btnCliActualizar;
    private JButton btnCliEliminar;
    private JButton btnCliLimpiar;

    // =========================================================================
    // COMPONENTES: CUENTAS BANCARIAS
    // =========================================================================
    private JTextField txtCtaId;
    private JTextField txtCtaNumero;
    private JComboBox<ClienteItemCombo> cmbCtaCliente;
    private JComboBox<TipoCuenta> cmbCtaTipo;
    private JTextField txtCtaSaldo;
    private JComboBox<EstadoCuenta> cmbCtaEstado;
    private JTable tblCuentas;
    private DefaultTableModel modeloTablaCuentas;
    private JButton btnCtaAperturar;
    private JButton btnCtaCambiarEstado;
    private JButton btnGenerarNum;
    private JButton btnCtaLimpiar;

    // =========================================================================
    // COMPONENTES: OPERACIONES Y TRANSACCIONES (Fase 2)
    // =========================================================================
    private JComboBox<String> cmbTxTipo;
    private JLabel lblTxCuentaOrigen;
    private JComboBox<CuentaItemCombo> cmbTxCuentaOrigen;
    private JLabel lblInfoCuentaOrigen;
    private JLabel lblTxCuentaDestino;
    private JComboBox<CuentaItemCombo> cmbTxCuentaDestino;
    private JLabel lblInfoCuentaDestino;
    private JTextField txtTxMonto;
    private JLabel lblTxMontoValidacion;
    private JTextField txtTxDescripcion;
    private JButton btnTxEjecutar;
    private JButton btnTxLimpiar;
    private JTextField txtTxBuscar;
    private JButton btnTxBuscar;
    private JButton btnTxLimpiarFiltro;
    private JTable tblTransacciones;
    private DefaultTableModel modeloTablaTransacciones;

    // =========================================================================
    // COMPONENTES: REPORTES Y AUDITORÍA (Fase 2)
    // =========================================================================
    private JLabel lblRepTotalDepositado;
    private JLabel lblRepTotalRetirado;
    private JLabel lblRepTotalTransferido;
    private JLabel lblRepSaldoCustodia;
    private JLabel lblRepTotalTx;
    private JComboBox<String> cmbRepTipoReporte;
    private JPanel pnlRepFiltroCuenta;
    private JComboBox<CuentaItemCombo> cmbRepCuenta;
    private JButton btnRepGenerar;
    private JButton btnRepVerLog;
    private JLabel lblRepContador;
    private JTable tblReportes;
    private DefaultTableModel modeloTablaReportes;

    public VentanaPrincipal() {
        this.servicio = new BancoServicio();
        inicializarVentana();
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarVentana() {
        setTitle("Banco Académico UDB — Sistema de Gestión Bancaria (POO901)");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1024, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel panelContenedor = new JPanel(new BorderLayout(10, 10));
        panelContenedor.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Encabezado
        panelContenedor.add(crearPanelEncabezado(), BorderLayout.NORTH);

        // Pestañas Principales
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("📊 Resumen General", crearPanelDashboard());
        tabbedPane.addTab("👥 Gestión de Clientes", crearPanelClientes());
        tabbedPane.addTab("💳 Cuentas Bancarias", crearPanelCuentas());
        tabbedPane.addTab("💸 Operaciones y Transacciones", crearPanelTransacciones());
        tabbedPane.addTab("📈 Reportes y Auditoría", crearPanelReportes());

        panelContenedor.add(tabbedPane, BorderLayout.CENTER);

        // Barra de estado inferior
        panelContenedor.add(crearBarraEstado(), BorderLayout.SOUTH);

        setContentPane(panelContenedor);
    }

    // =========================================================================
    // ENCABEZADO Y BARRA DE ESTADO
    // =========================================================================

    private JPanel crearPanelEncabezado() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(24, 43, 73));
        pnl.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitulo = new JLabel("BANCO ACADÉMICO UDB");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Ciclo 02/2026 · Cátedra POO901 — FASE 2: Transacciones Financieras ACID, Reportes y Bitácora Log4J");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(200, 215, 235));

        JPanel pnlTexto = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTexto.setOpaque(false);
        pnlTexto.add(lblTitulo);
        pnlTexto.add(lblSubtitulo);

        pnl.add(pnlTexto, BorderLayout.WEST);
        return pnl;
    }

    private JPanel crearBarraEstado() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(new EmptyBorder(6, 4, 4, 4));

        lblEstadoBD = new JLabel("Estado Conexión: Verificando...");
        lblEstadoBD.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblInfo = new JLabel("Plataforma Bancaria Académica · Fase 2 Activa — Preparado para Fase 3 (Web Servlets/JSP)");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        pnl.add(lblEstadoBD, BorderLayout.WEST);
        pnl.add(lblInfo, BorderLayout.EAST);
        return pnl;
    }

    // =========================================================================
    // PESTAÑA 1: DASHBOARD / RESUMEN GENERAL (Actualizado Fase 2)
    // =========================================================================

    private JPanel crearPanelDashboard() {
        JPanel pnl = new JPanel(new BorderLayout(15, 15));
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel de Métricas: 2 Filas
        JPanel pnlTarjetasContenedor = new JPanel(new GridLayout(2, 1, 10, 10));

        // Fila 1: Clientes y Cuentas
        JPanel pnlFila1 = new JPanel(new GridLayout(1, 5, 10, 10));
        lblTotalClientes = new JLabel("0", SwingConstants.CENTER);
        lblTotalCuentas = new JLabel("0", SwingConstants.CENTER);
        lblCuentasActivas = new JLabel("0", SwingConstants.CENTER);
        lblCuentasBloqueadas = new JLabel("0", SwingConstants.CENTER);
        lblSaldoGlobal = new JLabel("$0.00", SwingConstants.CENTER);

        pnlFila1.add(crearTarjetaMetrica("Clientes Totales", lblTotalClientes, new Color(41, 128, 185)));
        pnlFila1.add(crearTarjetaMetrica("Cuentas Totales", lblTotalCuentas, new Color(52, 73, 94)));
        pnlFila1.add(crearTarjetaMetrica("Cuentas Activas", lblCuentasActivas, new Color(39, 174, 96)));
        pnlFila1.add(crearTarjetaMetrica("Cuentas Bloqueadas", lblCuentasBloqueadas, new Color(192, 57, 43)));
        pnlFila1.add(crearTarjetaMetrica("Saldo en Custodia", lblSaldoGlobal, new Color(142, 68, 173)));

        // Fila 2: Transacciones y Movimientos Financieros
        JPanel pnlFila2 = new JPanel(new GridLayout(1, 5, 10, 10));
        lblDashTotalDepositado = new JLabel("$0.00", SwingConstants.CENTER);
        lblDashTotalRetirado = new JLabel("$0.00", SwingConstants.CENTER);
        lblDashTotalTransferido = new JLabel("$0.00", SwingConstants.CENTER);
        lblDashTotalTx = new JLabel("0", SwingConstants.CENTER);
        JLabel lblDashLog4j = new JLabel("ACTIVA", SwingConstants.CENTER);

        pnlFila2.add(crearTarjetaMetrica("Total Depositado", lblDashTotalDepositado, new Color(26, 188, 156)));
        pnlFila2.add(crearTarjetaMetrica("Total Retirado", lblDashTotalRetirado, new Color(230, 126, 34)));
        pnlFila2.add(crearTarjetaMetrica("Total Transferido", lblDashTotalTransferido, new Color(52, 152, 219)));
        pnlFila2.add(crearTarjetaMetrica("Total Transacciones", lblDashTotalTx, new Color(211, 84, 0)));
        pnlFila2.add(crearTarjetaMetrica("Auditoría Log4J 2", lblDashLog4j, new Color(44, 62, 80)));

        pnlTarjetasContenedor.add(pnlFila1);
        pnlTarjetasContenedor.add(pnlFila2);
        pnl.add(pnlTarjetasContenedor, BorderLayout.NORTH);

        // Panel Informativo Central
        JPanel pnlDetalle = new JPanel(new BorderLayout(10, 10));
        pnlDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Alcance y Estado del Sistema Bancario UDB",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));

        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setText(
                "BIENVENIDO AL SISTEMA DE GESTIÓN BANCARIA ACADÉMICA (BANCO ACADÉMICO UDB)\n\n" +
                "• FASE 1 (Completada): Gestión de Clientes y Cuentas Bancarias con persistencia en MySQL y JDBC.\n" +
                "  - CRUD integral de clientes con validaciones de DUI salvadoreño, correo institucional y teléfonos válidos.\n" +
                "  - Creación de cuentas corrientes y de ahorro con polimorfismo contable y rendimientos mensuales.\n\n" +
                "• FASE 2 (Actual y Operativa): Transacciones Financieras ACID, Auditoría y Reportes Gerenciales.\n" +
                "  - Operaciones reactivas: Depósitos, Retiros y Transferencias atómicas entre cuentas activas.\n" +
                "  - Control transaccional estricto: Bloqueo de registros (SELECT FOR UPDATE), commit y rollback ante anomalías.\n" +
                "  - Validación de saldos disponibles en tiempo real e impedimento de auto-transferencias.\n" +
                "  - Bitácora física centralizada con Apache Log4j 2 en logs/banco.log y visualizador interactivo modal.\n" +
                "  - Módulo de Reportes con métricas consolidadas de clientes y consulta de cuentas activas/bloqueadas.\n\n" +
                "• FASE 3 (Próxima): Migración a Plataforma Web con Servlets, JSP y JavaBeans sobre Apache Tomcat.\n" +
                "  - Reutilización sin fricciones de la capa de datos (DAOs) y reglas de negocio (BancoServicio).\n" +
                "  - Control de accesos por roles (Administrador, Cajero y Cliente Titular)."
        );
        pnlDetalle.add(new JScrollPane(txtInfo), BorderLayout.CENTER);

        JButton btnRefrescarDashboard = new JButton("🔄 Actualizar Métricas y Estadísticas");
        btnRefrescarDashboard.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefrescarDashboard.addActionListener(e -> {
            cargarEstadisticas();
            cargarMetricasReportes();
        });
        pnlDetalle.add(btnRefrescarDashboard, BorderLayout.SOUTH);

        pnl.add(pnlDetalle, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel lblValor, Color colorFondo) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(colorFondo);
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTit.setForeground(new Color(240, 240, 240));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblValor.setForeground(Color.WHITE);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PESTAÑA 2: GESTIÓN DE CLIENTES
    // =========================================================================

    private JPanel crearPanelClientes() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Formulario a la izquierda
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Formulario de Cliente",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        pnlForm.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCliId = new JTextField();
        txtCliId.setEditable(false);
        txtCliId.setVisible(false);

        txtCliCodigo = new JTextField(15);
        txtCliDui = new JTextField(15);
        txtCliNombre = new JTextField(15);
        txtCliTelefono = new JTextField(15);
        txtCliCorreo = new JTextField(15);
        txtCliDireccion = new JTextField(15);
        cmbCliEstado = new JComboBox<>(EstadoCliente.values());

        int fila = 0;
        agregarFilaForm(pnlForm, gbc, fila++, "Código:", txtCliCodigo);
        agregarFilaForm(pnlForm, gbc, fila++, "DUI (00000000-0):", txtCliDui);
        agregarFilaForm(pnlForm, gbc, fila++, "Nombre Completo:", txtCliNombre);
        agregarFilaForm(pnlForm, gbc, fila++, "Teléfono (0000-0000):", txtCliTelefono);
        agregarFilaForm(pnlForm, gbc, fila++, "Correo Electrónico:", txtCliCorreo);
        agregarFilaForm(pnlForm, gbc, fila++, "Dirección:", txtCliDireccion);
        agregarFilaForm(pnlForm, gbc, fila++, "Estado:", cmbCliEstado);

        // Botones de acción formulario
        JPanel pnlBotonesForm = new JPanel(new GridLayout(2, 2, 6, 6));
        btnCliGuardar = new JButton("💾 Guardar");
        btnCliActualizar = new JButton("✏️ Actualizar");
        btnCliEliminar = new JButton("🗑️ Eliminar");
        btnCliLimpiar = new JButton("🧹 Limpiar");

        btnCliGuardar.setBackground(new Color(39, 174, 96));
        btnCliGuardar.setForeground(Color.BLACK);
        btnCliActualizar.setEnabled(false);
        btnCliEliminar.setEnabled(false);

        pnlBotonesForm.add(btnCliGuardar);
        pnlBotonesForm.add(btnCliActualizar);
        pnlBotonesForm.add(btnCliEliminar);
        pnlBotonesForm.add(btnCliLimpiar);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        pnlForm.add(pnlBotonesForm, gbc);

        pnl.add(pnlForm, BorderLayout.WEST);

        // Tabla y Búsqueda a la derecha
        JPanel pnlTabla = new JPanel(new BorderLayout(8, 8));

        // Barra de búsqueda superior
        JPanel pnlBuscar = new JPanel(new BorderLayout(6, 6));
        pnlBuscar.setBorder(new EmptyBorder(4, 4, 4, 4));
        pnlBuscar.add(new JLabel("🔍 Buscar Cliente (Nombre, DUI o Código):"), BorderLayout.WEST);
        txtCliBuscar = new JTextField();
        pnlBuscar.add(txtCliBuscar, BorderLayout.CENTER);
        JButton btnBuscar = new JButton("Filtrar");
        JButton btnLimpiarFiltro = new JButton("Todos");
        JPanel pnlBotonesBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBotonesBusqueda.add(btnBuscar);
        pnlBotonesBusqueda.add(btnLimpiarFiltro);
        pnlBuscar.add(pnlBotonesBusqueda, BorderLayout.EAST);
        pnlTabla.add(pnlBuscar, BorderLayout.NORTH);

        // JTable
        String[] columnasClientes = {"ID", "Código", "DUI", "Nombre Completo", "Teléfono", "Correo", "Dirección", "Estado"};
        modeloTablaClientes = new DefaultTableModel(columnasClientes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblClientes = new JTable(modeloTablaClientes);
        tblClientes.setRowHeight(24);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlTabla.add(new JScrollPane(tblClientes), BorderLayout.CENTER);

        pnl.add(pnlTabla, BorderLayout.CENTER);

        // Eventos de Clientes
        btnCliGuardar.addActionListener(e -> guardarCliente());
        btnCliActualizar.addActionListener(e -> actualizarCliente());
        btnCliEliminar.addActionListener(e -> eliminarCliente());
        btnCliLimpiar.addActionListener(e -> limpiarFormularioCliente());
        btnBuscar.addActionListener(e -> filtrarClientes());
        txtCliBuscar.addActionListener(e -> filtrarClientes());
        btnLimpiarFiltro.addActionListener(e -> {
            txtCliBuscar.setText("");
            cargarTablaClientes();
        });

        tblClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarClienteSeleccionadoEnFormulario();
            }
        });

        return pnl;
    }

    private void agregarFilaForm(JPanel pnl, GridBagConstraints gbc, int fila, String etiqueta, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        pnl.add(comp, gbc);
    }

    // =========================================================================
    // PESTAÑA 3: CUENTAS BANCARIAS
    // =========================================================================

    private JPanel crearPanelCuentas() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Formulario Cuentas
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Apertura y Gestión de Cuenta",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        pnlForm.setPreferredSize(new Dimension(410, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCtaId = new JTextField();
        txtCtaId.setVisible(false);

        txtCtaNumero = new JTextField(15);
        cmbCtaCliente = new JComboBox<>();
        cmbCtaTipo = new JComboBox<>(TipoCuenta.values());
        txtCtaSaldo = new JTextField("0.00", 15);
        cmbCtaEstado = new JComboBox<>(EstadoCuenta.values());

        int fila = 0;
        agregarFilaForm(pnlForm, gbc, fila++, "N° de Cuenta:", txtCtaNumero);
        agregarFilaForm(pnlForm, gbc, fila++, "Cliente Titular:", cmbCtaCliente);
        agregarFilaForm(pnlForm, gbc, fila++, "Tipo de Cuenta:", cmbCtaTipo);
        agregarFilaForm(pnlForm, gbc, fila++, "Saldo Inicial ($):", txtCtaSaldo);
        agregarFilaForm(pnlForm, gbc, fila++, "Estado de Cuenta:", cmbCtaEstado);

        // Botones Formulario Cuentas
        JPanel pnlBotones = new JPanel(new GridLayout(2, 2, 6, 6));
        btnCtaAperturar = new JButton("✨ Aperturar Cuenta");
        btnCtaCambiarEstado = new JButton("🔄 Cambiar Estado");
        btnGenerarNum = new JButton("🎲 Generar N°");
        btnCtaLimpiar = new JButton("🧹 Limpiar");

        btnCtaCambiarEstado.setEnabled(false);

        pnlBotones.add(btnCtaAperturar);
        pnlBotones.add(btnCtaCambiarEstado);
        pnlBotones.add(btnGenerarNum);
        pnlBotones.add(btnCtaLimpiar);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 6, 6);
        pnlForm.add(pnlBotones, gbc);

        pnl.add(pnlForm, BorderLayout.WEST);

        // Tabla de Cuentas
        JPanel pnlTabla = new JPanel(new BorderLayout(8, 8));
        String[] columnasCuentas = {"ID", "N° Cuenta", "Tipo", "Cliente Titular", "Saldo", "Rend. Mensual (POO)", "Estado", "Fecha Apertura"};
        modeloTablaCuentas = new DefaultTableModel(columnasCuentas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCuentas = new JTable(modeloTablaCuentas);
        tblCuentas.setRowHeight(24);
        tblCuentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlTabla.add(new JScrollPane(tblCuentas), BorderLayout.CENTER);

        pnl.add(pnlTabla, BorderLayout.CENTER);

        // Eventos
        btnCtaAperturar.addActionListener(e -> aperturarCuenta());
        btnCtaCambiarEstado.addActionListener(e -> cambiarEstadoCuenta());
        btnGenerarNum.addActionListener(e -> generarNumeroCuentaAleatorio());
        btnCtaLimpiar.addActionListener(e -> limpiarFormularioCuenta());

        tblCuentas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarCuentaSeleccionada();
            }
        });

        return pnl;
    }

    // =========================================================================
    // PESTAÑA 4: OPERACIONES Y TRANSACCIONES FINANCIERAS (Fase 2)
    // =========================================================================

    private JPanel crearPanelTransacciones() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Formulario de Transacción a la Izquierda
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Ejecutar Operación Financiera",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        pnlForm.setPreferredSize(new Dimension(420, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbTxTipo = new JComboBox<>(new String[]{"Depósito", "Retiro", "Transferencia"});
        cmbTxTipo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblTxCuentaOrigen = new JLabel("Cuenta a Depositar:");
        lblTxCuentaOrigen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTxCuentaOrigen = new JComboBox<>();

        lblInfoCuentaOrigen = new JLabel("Seleccione una cuenta...");
        lblInfoCuentaOrigen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoCuentaOrigen.setForeground(new Color(50, 90, 150));

        lblTxCuentaDestino = new JLabel("Cuenta Destino (Crédito):");
        lblTxCuentaDestino.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTxCuentaDestino = new JComboBox<>();

        lblInfoCuentaDestino = new JLabel("Seleccione una cuenta destino...");
        lblInfoCuentaDestino.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoCuentaDestino.setForeground(new Color(50, 90, 150));

        // Inicialmente ocultar destino porque por defecto es Depósito
        lblTxCuentaDestino.setVisible(false);
        cmbTxCuentaDestino.setVisible(false);
        lblInfoCuentaDestino.setVisible(false);

        txtTxMonto = new JTextField("0.00", 15);
        txtTxMonto.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblTxMontoValidacion = new JLabel("Ingrese un monto mayor a $0.00");
        lblTxMontoValidacion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTxMontoValidacion.setForeground(Color.GRAY);

        txtTxDescripcion = new JTextField(15);
        txtTxDescripcion.setToolTipText("Concepto o motivo de la transacción (opcional)");

        btnTxEjecutar = new JButton("⚡ Procesar Operación");
        btnTxEjecutar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTxEjecutar.setBackground(new Color(39, 174, 96));
        btnTxEjecutar.setForeground(Color.BLACK);
        btnTxEjecutar.setEnabled(false);

        btnTxLimpiar = new JButton("🧹 Limpiar Campos");
        btnTxLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Construir formulario con GridBagLayout
        int fila = 0;
        agregarFilaForm(pnlForm, gbc, fila++, "Tipo de Transacción:", cmbTxTipo);

        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        pnlForm.add(lblTxCuentaOrigen, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cmbTxCuentaOrigen, gbc);
        fila++;

        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;
        pnlForm.add(lblInfoCuentaOrigen, gbc);
        fila++;
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        pnlForm.add(lblTxCuentaDestino, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cmbTxCuentaDestino, gbc);
        fila++;

        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;
        pnlForm.add(lblInfoCuentaDestino, gbc);
        fila++;
        gbc.gridwidth = 1;

        agregarFilaForm(pnlForm, gbc, fila++, "Monto Monetario ($):", txtTxMonto);

        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;
        pnlForm.add(lblTxMontoValidacion, gbc);
        fila++;
        gbc.gridwidth = 1;

        agregarFilaForm(pnlForm, gbc, fila++, "Descripción / Concepto:", txtTxDescripcion);

        JPanel pnlBotonesTx = new JPanel(new GridLayout(1, 2, 8, 8));
        pnlBotonesTx.add(btnTxEjecutar);
        pnlBotonesTx.add(btnTxLimpiar);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 6, 6);
        pnlForm.add(pnlBotonesTx, gbc);

        pnl.add(pnlForm, BorderLayout.WEST);

        // Tabla y Búsqueda de Transacciones a la Derecha
        JPanel pnlTabla = new JPanel(new BorderLayout(8, 8));

        // Barra de búsqueda de movimientos
        JPanel pnlBuscar = new JPanel(new BorderLayout(6, 6));
        pnlBuscar.setBorder(new EmptyBorder(4, 4, 4, 4));
        pnlBuscar.add(new JLabel("🔍 Buscar Transacción por ID o Código (ej. TX-DEP-...):"), BorderLayout.WEST);
        txtTxBuscar = new JTextField();
        pnlBuscar.add(txtTxBuscar, BorderLayout.CENTER);

        btnTxBuscar = new JButton("Buscar");
        btnTxLimpiarFiltro = new JButton("Todas");
        JPanel pnlBotonesBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBotonesBusqueda.add(btnTxBuscar);
        pnlBotonesBusqueda.add(btnTxLimpiarFiltro);
        pnlBuscar.add(pnlBotonesBusqueda, BorderLayout.EAST);
        pnlTabla.add(pnlBuscar, BorderLayout.NORTH);

        // JTable de Historial
        String[] colTx = {"ID", "Código", "Tipo", "Cuenta Origen", "Cuenta Destino", "Monto", "Saldo Resultante", "Fecha / Hora", "Descripción"};
        modeloTablaTransacciones = new DefaultTableModel(colTx, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTransacciones = new JTable(modeloTablaTransacciones);
        tblTransacciones.setRowHeight(24);
        tblTransacciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlTabla.add(new JScrollPane(tblTransacciones), BorderLayout.CENTER);

        pnl.add(pnlTabla, BorderLayout.CENTER);

        // Reactividad y Eventos de Transacciones
        cmbTxTipo.addActionListener(e -> {
            actualizarVisibilidadTipoTransaccion();
            actualizarDetalleCuentasTx();
            validarFormularioTransaccion();
        });

        cmbTxCuentaOrigen.addActionListener(e -> {
            actualizarDetalleCuentasTx();
            validarFormularioTransaccion();
        });

        cmbTxCuentaDestino.addActionListener(e -> {
            actualizarDetalleCuentasTx();
            validarFormularioTransaccion();
        });

        txtTxMonto.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarFormularioTransaccion(); }
            @Override public void removeUpdate(DocumentEvent e) { validarFormularioTransaccion(); }
            @Override public void changedUpdate(DocumentEvent e) { validarFormularioTransaccion(); }
        });

        btnTxEjecutar.addActionListener(e -> ejecutarTransaccionConConfirmacion());
        btnTxLimpiar.addActionListener(e -> limpiarFormularioTransaccion());

        btnTxBuscar.addActionListener(e -> buscarTransaccion());
        txtTxBuscar.addActionListener(e -> buscarTransaccion());
        btnTxLimpiarFiltro.addActionListener(e -> {
            txtTxBuscar.setText("");
            cargarTablaTransacciones();
        });

        return pnl;
    }

    // =========================================================================
    // PESTAÑA 5: REPORTES Y AUDITORÍA (Fase 2)
    // =========================================================================

    private JPanel crearPanelReportes() {
        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Tarjetas métricas superiores con los consolidados contables requeridos
        JPanel pnlTarjetas = new JPanel(new GridLayout(1, 5, 10, 10));

        lblRepTotalDepositado = new JLabel("$0.00", SwingConstants.CENTER);
        lblRepTotalRetirado = new JLabel("$0.00", SwingConstants.CENTER);
        lblRepTotalTransferido = new JLabel("$0.00", SwingConstants.CENTER);
        lblRepSaldoCustodia = new JLabel("$0.00", SwingConstants.CENTER);
        lblRepTotalTx = new JLabel("0", SwingConstants.CENTER);

        pnlTarjetas.add(crearTarjetaMetrica("Total Depositado ($)", lblRepTotalDepositado, new Color(39, 174, 96)));
        pnlTarjetas.add(crearTarjetaMetrica("Total Retirado ($)", lblRepTotalRetirado, new Color(192, 57, 43)));
        pnlTarjetas.add(crearTarjetaMetrica("Total Transferido ($)", lblRepTotalTransferido, new Color(41, 128, 185)));
        pnlTarjetas.add(crearTarjetaMetrica("Total en Custodia ($)", lblRepSaldoCustodia, new Color(142, 68, 173)));
        pnlTarjetas.add(crearTarjetaMetrica("Total Transacciones", lblRepTotalTx, new Color(211, 84, 0)));

        pnl.add(pnlTarjetas, BorderLayout.NORTH);

        // 2. Contenedor Central: Barra de control de reportes y JTable interactiva
        JPanel pnlCentro = new JPanel(new BorderLayout(8, 8));

        // Barra superior de filtros y acciones
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlControles.setBorder(BorderFactory.createEtchedBorder());

        pnlControles.add(new JLabel("Tipo de Reporte:"));
        cmbRepTipoReporte = new JComboBox<>(new String[]{
                "1. Cuentas Bancarias Activas",
                "2. Cuentas Bancarias Bloqueadas",
                "3. Clientes Registrados con Saldo Consolidado",
                "4. Historial General de Transacciones",
                "5. Transacciones por Cuenta Específica"
        });
        cmbRepTipoReporte.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlControles.add(cmbRepTipoReporte);

        pnlRepFiltroCuenta = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlRepFiltroCuenta.add(new JLabel("Cuenta:"));
        cmbRepCuenta = new JComboBox<>();
        cmbRepCuenta.setPreferredSize(new Dimension(280, 26));
        pnlRepFiltroCuenta.add(cmbRepCuenta);
        pnlRepFiltroCuenta.setVisible(false); // Visible únicamente para opción 5
        pnlControles.add(pnlRepFiltroCuenta);

        btnRepGenerar = new JButton("🔄 Generar Reporte");
        btnRepGenerar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlControles.add(btnRepGenerar);

        btnRepVerLog = new JButton("📜 Ver Bitácora (Log4J)");
        btnRepVerLog.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRepVerLog.setBackground(new Color(24, 43, 73));
        btnRepVerLog.setForeground(Color.BLACK);
        pnlControles.add(btnRepVerLog);

        lblRepContador = new JLabel("Registros: 0");
        lblRepContador.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRepContador.setForeground(Color.DARK_GRAY);
        pnlControles.add(lblRepContador);

        pnlCentro.add(pnlControles, BorderLayout.NORTH);

        // Tabla interactiva dinámica para mostrar las columnas y datos adecuados
        modeloTablaReportes = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblReportes = new JTable(modeloTablaReportes);
        tblReportes.setRowHeight(24);
        tblReportes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlCentro.add(new JScrollPane(tblReportes), BorderLayout.CENTER);

        pnl.add(pnlCentro, BorderLayout.CENTER);

        // Eventos
        cmbRepTipoReporte.addActionListener(e -> {
            int sel = cmbRepTipoReporte.getSelectedIndex();
            pnlRepFiltroCuenta.setVisible(sel == 4);
            generarReporteSeleccionado();
        });

        btnRepGenerar.addActionListener(e -> generarReporteSeleccionado());
        btnRepVerLog.addActionListener(e -> mostrarDialogoBitacora());

        return pnl;
    }

    // =========================================================================
    // LÓGICA DE CONTROL Y EVENTOS: CLIENTES
    // =========================================================================

    private void cargarDatosIniciales() {
        actualizarEstadoConexion();
        cargarTablaClientes();
        cargarComboClientes();
        cargarTablaCuentas();
        cargarCombosCuentas();
        cargarTablaTransacciones();
        cargarEstadisticas();
        cargarMetricasReportes();
        generarReporteSeleccionado();
    }

    private void actualizarEstadoConexion() {
        boolean ok = ConexionBD.probarConexion();
        if (ok) {
            lblEstadoBD.setText("● Conectado a BD: " + ConexionBD.getPerfilActivo().getNombre() + " (Puerto " + ConexionBD.getPerfilActivo().getPuerto() + ")");
            lblEstadoBD.setForeground(new Color(25, 135, 84));
        } else {
            lblEstadoBD.setText("○ Desconectado de BD");
            lblEstadoBD.setForeground(new Color(220, 53, 69));
        }
    }

    private void cargarTablaClientes() {
        try {
            modeloTablaClientes.setRowCount(0);
            List<Cliente> lista = servicio.listarClientes();
            for (Cliente c : lista) {
                modeloTablaClientes.addRow(new Object[]{
                        c.getId(),
                        c.getCodigoCliente(),
                        c.getDui(),
                        c.getNombreCompleto(),
                        c.getTelefono(),
                        c.getCorreoElectronico(),
                        c.getDireccion(),
                        c.getEstado()
                });
            }
        } catch (BancoException e) {
            mostrarError("Error al cargar clientes", e.getMessage());
        }
    }

    private void cargarComboClientes() {
        cmbCtaCliente.removeAllItems();
        try {
            List<Cliente> lista = servicio.listarClientes();
            for (Cliente c : lista) {
                if (c.isActivo()) {
                    cmbCtaCliente.addItem(new ClienteItemCombo(c));
                }
            }
        } catch (BancoException e) {
            logger.warn("No se pudo cargar la lista de clientes activos en el combo de cuentas: {}", e.getMessage());
        }
    }

    private void guardarCliente() {
        try {
            Cliente nuevo = new Cliente();
            nuevo.setCodigoCliente(txtCliCodigo.getText().trim());
            nuevo.setDui(txtCliDui.getText().trim());
            nuevo.setNombreCompleto(txtCliNombre.getText().trim());
            nuevo.setTelefono(txtCliTelefono.getText().trim());
            nuevo.setCorreoElectronico(txtCliCorreo.getText().trim());
            nuevo.setDireccion(txtCliDireccion.getText().trim());
            nuevo.setEstado((EstadoCliente) cmbCliEstado.getSelectedItem());

            servicio.registrarCliente(nuevo);

            JOptionPane.showMessageDialog(this,
                    "¡Cliente '" + nuevo.getNombreCompleto() + "' registrado exitosamente!",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormularioCliente();
            cargarTablaClientes();
            cargarComboClientes();
            cargarEstadisticas();
            cargarMetricasReportes();
        } catch (BancoException ex) {
            mostrarError("No se pudo registrar el cliente", ex.getMessage());
        }
    }

    private void actualizarCliente() {
        try {
            int id = Integer.parseInt(txtCliId.getText().trim());
            Cliente cliente = new Cliente();
            cliente.setId(id);
            cliente.setCodigoCliente(txtCliCodigo.getText().trim());
            cliente.setDui(txtCliDui.getText().trim());
            cliente.setNombreCompleto(txtCliNombre.getText().trim());
            cliente.setTelefono(txtCliTelefono.getText().trim());
            cliente.setCorreoElectronico(txtCliCorreo.getText().trim());
            cliente.setDireccion(txtCliDireccion.getText().trim());
            cliente.setEstado((EstadoCliente) cmbCliEstado.getSelectedItem());

            servicio.actualizarCliente(cliente);

            JOptionPane.showMessageDialog(this,
                    "Cliente actualizado exitosamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormularioCliente();
            cargarTablaClientes();
            cargarComboClientes();
            cargarTablaCuentas();
            cargarCombosCuentas();
            cargarEstadisticas();
            cargarMetricasReportes();
        } catch (Exception ex) {
            mostrarError("Error al actualizar cliente", ex.getMessage());
        }
    }

    private void eliminarCliente() {
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) {
            mostrarAdvertencia("Seleccione un cliente en la tabla para eliminar.");
            return;
        }

        int id = (int) modeloTablaClientes.getValueAt(fila, 0);
        String nombre = (String) modeloTablaClientes.getValueAt(fila, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al cliente '" + nombre + "'?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                servicio.eliminarCliente(id);
                JOptionPane.showMessageDialog(this,
                        "Cliente eliminado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormularioCliente();
                cargarTablaClientes();
                cargarComboClientes();
                cargarEstadisticas();
                cargarMetricasReportes();
            } catch (BancoException ex) {
                mostrarError("No se puede eliminar", ex.getMessage());
            }
        }
    }

    private void filtrarClientes() {
        String termino = txtCliBuscar.getText().trim();
        try {
            modeloTablaClientes.setRowCount(0);
            List<Cliente> filtrados = servicio.buscarClientes(termino);
            for (Cliente c : filtrados) {
                modeloTablaClientes.addRow(new Object[]{
                        c.getId(),
                        c.getCodigoCliente(),
                        c.getDui(),
                        c.getNombreCompleto(),
                        c.getTelefono(),
                        c.getCorreoElectronico(),
                        c.getDireccion(),
                        c.getEstado()
                });
            }
        } catch (BancoException e) {
            mostrarError("Error en filtro de clientes", e.getMessage());
        }
    }

    private void cargarClienteSeleccionadoEnFormulario() {
        int fila = tblClientes.getSelectedRow();
        if (fila >= 0) {
            txtCliId.setText(modeloTablaClientes.getValueAt(fila, 0).toString());
            txtCliCodigo.setText(modeloTablaClientes.getValueAt(fila, 1).toString());
            txtCliDui.setText(modeloTablaClientes.getValueAt(fila, 2).toString());
            txtCliNombre.setText(modeloTablaClientes.getValueAt(fila, 3).toString());
            txtCliTelefono.setText(modeloTablaClientes.getValueAt(fila, 4).toString());
            txtCliCorreo.setText(modeloTablaClientes.getValueAt(fila, 5).toString());
            txtCliDireccion.setText(modeloTablaClientes.getValueAt(fila, 6).toString());
            cmbCliEstado.setSelectedItem(modeloTablaClientes.getValueAt(fila, 7));

            btnCliGuardar.setEnabled(false);
            btnCliActualizar.setEnabled(true);
            btnCliEliminar.setEnabled(true);
        }
    }

    private void limpiarFormularioCliente() {
        txtCliId.setText("");
        txtCliCodigo.setText("");
        txtCliDui.setText("");
        txtCliNombre.setText("");
        txtCliTelefono.setText("");
        txtCliCorreo.setText("");
        txtCliDireccion.setText("");
        cmbCliEstado.setSelectedItem(EstadoCliente.ACTIVO);

        tblClientes.clearSelection();
        btnCliGuardar.setEnabled(true);
        btnCliActualizar.setEnabled(false);
        btnCliEliminar.setEnabled(false);
    }

    // =========================================================================
    // LÓGICA DE CONTROL Y EVENTOS: CUENTAS BANCARIAS
    // =========================================================================

    private void cargarTablaCuentas() {
        try {
            modeloTablaCuentas.setRowCount(0);
            List<Cuenta> lista = servicio.listarCuentas();
            for (Cuenta c : lista) {
                String titular = (c.getCliente() != null)
                        ? c.getCliente().getNombreCompleto() + " (" + c.getCliente().getCodigoCliente() + ")"
                        : "ID: " + c.getIdCliente();

                modeloTablaCuentas.addRow(new Object[]{
                        c.getIdCuenta(),
                        c.getNumeroCuenta(),
                        c.getTipoCuenta().getNombreVisual(),
                        titular,
                        Formateador.aMoneda(c.getSaldo()),
                        Formateador.aMoneda(c.calcularRendimientoEstimadoMensual()),
                        c.getEstado(),
                        Formateador.aFecha(c.getFechaApertura())
                });
            }
        } catch (BancoException e) {
            mostrarError("Error al cargar cuentas", e.getMessage());
        }
    }

    private void aperturarCuenta() {
        try {
            ClienteItemCombo item = (ClienteItemCombo) cmbCtaCliente.getSelectedItem();
            if (item == null) {
                mostrarAdvertencia("Debe seleccionar un cliente activo como titular de la cuenta.");
                return;
            }

            TipoCuenta tipo = (TipoCuenta) cmbCtaTipo.getSelectedItem();
            String num = txtCtaNumero.getText().trim();
            double saldoInicial = Double.parseDouble(txtCtaSaldo.getText().trim());

            Cuenta nueva;
            if (tipo == TipoCuenta.AHORRO) {
                nueva = new CuentaAhorro(num, item.getCliente().getId(), saldoInicial);
            } else {
                nueva = new CuentaCorriente(num, item.getCliente().getId(), saldoInicial);
            }
            nueva.setEstado((EstadoCuenta) cmbCtaEstado.getSelectedItem());

            servicio.aperturarCuenta(nueva);

            JOptionPane.showMessageDialog(this,
                    "¡Cuenta bancaria '" + nueva.getNumeroCuenta() + "' aperturada exitosamente para " +
                            item.getCliente().getNombreCompleto() + "!",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormularioCuenta();
            cargarTablaCuentas();
            cargarCombosCuentas();
            cargarEstadisticas();
            cargarMetricasReportes();
        } catch (NumberFormatException ex) {
            mostrarError("Saldo inválido", "El saldo inicial debe ser un número decimal válido (ej. 150.00).");
        } catch (BancoException ex) {
            mostrarError("No se pudo aperturar la cuenta", ex.getMessage());
        }
    }

    private void cambiarEstadoCuenta() {
        try {
            int idCuenta = Integer.parseInt(txtCtaId.getText().trim());
            EstadoCuenta nuevoEstado = (EstadoCuenta) cmbCtaEstado.getSelectedItem();

            servicio.cambiarEstadoCuenta(idCuenta, nuevoEstado);

            JOptionPane.showMessageDialog(this,
                    "Estado de la cuenta actualizado a: " + nuevoEstado,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormularioCuenta();
            cargarTablaCuentas();
            cargarCombosCuentas();
            cargarEstadisticas();
            cargarMetricasReportes();
        } catch (Exception ex) {
            mostrarError("Error al cambiar estado", ex.getMessage());
        }
    }

    private void generarNumeroCuentaAleatorio() {
        int anio = java.time.Year.now().getValue();
        int aleatorio = (int) (Math.random() * 90000) + 10000;
        txtCtaNumero.setText("CTA-" + anio + "-" + aleatorio);
    }

    private void cargarCuentaSeleccionada() {
        int fila = tblCuentas.getSelectedRow();
        if (fila >= 0) {
            txtCtaId.setText(modeloTablaCuentas.getValueAt(fila, 0).toString());
            txtCtaNumero.setText(modeloTablaCuentas.getValueAt(fila, 1).toString());
            txtCtaNumero.setEditable(false);

            String titularVisual = modeloTablaCuentas.getValueAt(fila, 3).toString();
            boolean encontrado = false;
            for (int i = 0; i < cmbCtaCliente.getItemCount(); i++) {
                ClienteItemCombo item = cmbCtaCliente.getItemAt(i);
                if (titularVisual.contains(item.getCliente().getCodigoCliente()) ||
                    titularVisual.contains(item.getCliente().getNombreCompleto())) {
                    cmbCtaCliente.setSelectedIndex(i);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                try {
                    int idCuenta = Integer.parseInt(txtCtaId.getText().trim());
                    Cuenta c = servicio.listarCuentas().stream()
                            .filter(cta -> cta.getIdCuenta() == idCuenta)
                            .findFirst().orElse(null);
                    if (c != null && c.getCliente() != null) {
                        ClienteItemCombo itemTemp = new ClienteItemCombo(c.getCliente());
                        cmbCtaCliente.addItem(itemTemp);
                        cmbCtaCliente.setSelectedItem(itemTemp);
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo preseleccionar el titular de la cuenta ID='{}': {}", txtCtaId.getText(), e.getMessage());
                }
            }
            cmbCtaCliente.setEnabled(false);
            cmbCtaCliente.setToolTipText("El cliente titular no se puede modificar una vez creada la cuenta.");

            String tipoVisual = modeloTablaCuentas.getValueAt(fila, 2).toString();
            if (tipoVisual.contains("Ahorro")) {
                cmbCtaTipo.setSelectedItem(TipoCuenta.AHORRO);
            } else {
                cmbCtaTipo.setSelectedItem(TipoCuenta.CORRIENTE);
            }
            cmbCtaTipo.setEnabled(false);
            cmbCtaTipo.setToolTipText("El tipo de cuenta no se puede alterar.");

            String saldoActual = modeloTablaCuentas.getValueAt(fila, 4).toString();
            txtCtaSaldo.setText(saldoActual.replace("$", "").replace(",", "").trim());
            txtCtaSaldo.setEditable(false);
            txtCtaSaldo.setToolTipText("El saldo actual solo cambia mediante transacciones financieras.");

            EstadoCuenta est = (EstadoCuenta) modeloTablaCuentas.getValueAt(fila, 6);
            cmbCtaEstado.setSelectedItem(est);
            cmbCtaEstado.setEnabled(true);

            btnCtaAperturar.setEnabled(false);
            btnGenerarNum.setEnabled(false);
            btnCtaCambiarEstado.setEnabled(true);
        }
    }

    private void limpiarFormularioCuenta() {
        txtCtaId.setText("");
        txtCtaNumero.setText("");
        txtCtaNumero.setEditable(true);

        cargarComboClientes();
        cmbCtaCliente.setEnabled(true);
        cmbCtaCliente.setToolTipText("Seleccione el cliente titular de la cuenta");

        cmbCtaTipo.setEnabled(true);
        cmbCtaTipo.setSelectedItem(TipoCuenta.AHORRO);

        txtCtaSaldo.setText("0.00");
        txtCtaSaldo.setEditable(true);
        txtCtaSaldo.setToolTipText("Monto con el que se abre la cuenta bancaria");

        cmbCtaEstado.setEnabled(true);
        cmbCtaEstado.setSelectedItem(EstadoCuenta.ACTIVA);

        tblCuentas.clearSelection();
        btnCtaAperturar.setEnabled(true);
        btnGenerarNum.setEnabled(true);
        btnCtaCambiarEstado.setEnabled(false);
    }

    // =========================================================================
    // LÓGICA DE CONTROL Y EVENTOS: OPERACIONES Y TRANSACCIONES (Fase 2)
    // =========================================================================

    private void cargarCombosCuentas() {
        CuentaItemCombo selOrigen = (CuentaItemCombo) cmbTxCuentaOrigen.getSelectedItem();
        CuentaItemCombo selDestino = (CuentaItemCombo) cmbTxCuentaDestino.getSelectedItem();
        CuentaItemCombo selRep = (CuentaItemCombo) cmbRepCuenta.getSelectedItem();

        cmbTxCuentaOrigen.removeAllItems();
        cmbTxCuentaDestino.removeAllItems();
        cmbRepCuenta.removeAllItems();

        try {
            List<Cuenta> todas = servicio.listarCuentas();
            for (Cuenta c : todas) {
                CuentaItemCombo itemRep = new CuentaItemCombo(c);
                cmbRepCuenta.addItem(itemRep);

                // En transacciones permitimos seleccionar cuentas activas para operar
                if (c.puedeOperar()) {
                    cmbTxCuentaOrigen.addItem(new CuentaItemCombo(c));
                    cmbTxCuentaDestino.addItem(new CuentaItemCombo(c));
                }
            }
        } catch (BancoException e) {
            logger.warn("No se pudieron cargar las cuentas en los combos de transacciones/reportes: {}", e.getMessage());
        }

        // Seleccionar una cuenta de destino distinta a la de origen por comodidad si es posible
        if (cmbTxCuentaDestino.getItemCount() > 1) {
            cmbTxCuentaDestino.setSelectedIndex(1);
        }

        actualizarDetalleCuentasTx();
        validarFormularioTransaccion();
    }

    private void actualizarVisibilidadTipoTransaccion() {
        String tipo = (String) cmbTxTipo.getSelectedItem();
        boolean esTransferencia = "Transferencia".equalsIgnoreCase(tipo);

        if ("Depósito".equalsIgnoreCase(tipo)) {
            lblTxCuentaOrigen.setText("Cuenta a Depositar (Crédito):");
        } else if ("Retiro".equalsIgnoreCase(tipo)) {
            lblTxCuentaOrigen.setText("Cuenta a Debitar (Origen):");
        } else {
            lblTxCuentaOrigen.setText("Cuenta Origen (Débito):");
        }

        lblTxCuentaDestino.setVisible(esTransferencia);
        cmbTxCuentaDestino.setVisible(esTransferencia);
        lblInfoCuentaDestino.setVisible(esTransferencia);

        // Revalidar el formulario para actualizar el layout reactivamente
        if (lblTxCuentaDestino.getParent() != null) {
            lblTxCuentaDestino.getParent().revalidate();
            lblTxCuentaDestino.getParent().repaint();
        }
    }

    private void actualizarDetalleCuentasTx() {
        CuentaItemCombo itemOrigen = (CuentaItemCombo) cmbTxCuentaOrigen.getSelectedItem();
        if (itemOrigen != null && itemOrigen.getCuenta() != null) {
            Cuenta c = itemOrigen.getCuenta();
            String titular = (c.getCliente() != null) ? c.getCliente().getNombreCompleto() : "Cliente #" + c.getIdCliente();
            lblInfoCuentaOrigen.setText("Titular: " + titular + " | Saldo: " + Formateador.aMoneda(c.getSaldo()) + " | " + c.getEstado());
        } else {
            lblInfoCuentaOrigen.setText("No hay cuenta activa seleccionada");
        }

        CuentaItemCombo itemDestino = (CuentaItemCombo) cmbTxCuentaDestino.getSelectedItem();
        if (itemDestino != null && itemDestino.getCuenta() != null) {
            Cuenta c = itemDestino.getCuenta();
            String titular = (c.getCliente() != null) ? c.getCliente().getNombreCompleto() : "Cliente #" + c.getIdCliente();
            lblInfoCuentaDestino.setText("Titular: " + titular + " | Saldo: " + Formateador.aMoneda(c.getSaldo()) + " | " + c.getEstado());
        } else {
            lblInfoCuentaDestino.setText("No hay cuenta destino seleccionada");
        }
    }

    private void validarFormularioTransaccion() {
        String tipo = (String) cmbTxTipo.getSelectedItem();
        CuentaItemCombo itemOrigen = (CuentaItemCombo) cmbTxCuentaOrigen.getSelectedItem();

        if (itemOrigen == null || itemOrigen.getCuenta() == null) {
            lblTxMontoValidacion.setText("⚠️ Debe existir al menos una cuenta activa para operar.");
            lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
            btnTxEjecutar.setEnabled(false);
            return;
        }

        Cuenta ctaOrigen = itemOrigen.getCuenta();

        if ("Transferencia".equalsIgnoreCase(tipo)) {
            CuentaItemCombo itemDestino = (CuentaItemCombo) cmbTxCuentaDestino.getSelectedItem();
            if (itemDestino == null || itemDestino.getCuenta() == null) {
                lblTxMontoValidacion.setText("⚠️ Seleccione una cuenta destino.");
                lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
                btnTxEjecutar.setEnabled(false);
                return;
            }
            Cuenta ctaDestino = itemDestino.getCuenta();
            if (ctaOrigen.getIdCuenta() == ctaDestino.getIdCuenta()) {
                lblTxMontoValidacion.setText("⚠️ Cuenta origen y destino no pueden ser iguales.");
                lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
                btnTxEjecutar.setEnabled(false);
                return;
            }
        }

        String strMonto = txtTxMonto.getText().trim();
        if (strMonto.isEmpty()) {
            lblTxMontoValidacion.setText("Ingrese un monto numérico mayor a $0.00");
            lblTxMontoValidacion.setForeground(Color.GRAY);
            btnTxEjecutar.setEnabled(false);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(strMonto);
        } catch (NumberFormatException e) {
            lblTxMontoValidacion.setText("⚠️ Ingrese un valor decimal válido (ej. 125.50).");
            lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
            btnTxEjecutar.setEnabled(false);
            return;
        }

        if (monto <= 0.0) {
            lblTxMontoValidacion.setText("⚠️ El monto debe ser estrictamente mayor a $0.00.");
            lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
            btnTxEjecutar.setEnabled(false);
            return;
        }

        if ("Retiro".equalsIgnoreCase(tipo) || "Transferencia".equalsIgnoreCase(tipo)) {
            if (ctaOrigen.getSaldo() < monto) {
                lblTxMontoValidacion.setText("⚠️ Fondos insuficientes (Saldo disponible: " + Formateador.aMoneda(ctaOrigen.getSaldo()) + ")");
                lblTxMontoValidacion.setForeground(new Color(220, 53, 69));
                btnTxEjecutar.setEnabled(false);
                return;
            }
        }

        lblTxMontoValidacion.setText("✓ Transacción válida lista para ejecutarse.");
        lblTxMontoValidacion.setForeground(new Color(25, 135, 84));
        btnTxEjecutar.setEnabled(true);
    }

    private void ejecutarTransaccionConConfirmacion() {
        String tipo = (String) cmbTxTipo.getSelectedItem();
        CuentaItemCombo itemOrigen = (CuentaItemCombo) cmbTxCuentaOrigen.getSelectedItem();
        CuentaItemCombo itemDestino = (CuentaItemCombo) cmbTxCuentaDestino.getSelectedItem();

        if (itemOrigen == null || itemOrigen.getCuenta() == null) {
            mostrarAdvertencia("Seleccione una cuenta válida.");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(txtTxMonto.getText().trim());
            Validador.validarMontoTransaccion(monto);
        } catch (Exception e) {
            mostrarError("Monto inválido", e.getMessage());
            return;
        }

        String descripcion = txtTxDescripcion.getText().trim();
        Cuenta ctaOrigen = itemOrigen.getCuenta();
        boolean esTransferencia = "Transferencia".equalsIgnoreCase(tipo);

        // Ventana Modal de Confirmación
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append("¿Está seguro de confirmar la siguiente operación bancaria?\n\n");
        sbMsg.append("• Tipo de Transacción: ").append(tipo.toUpperCase()).append("\n");
        sbMsg.append("• Cuenta Origen: ").append(ctaOrigen.getNumeroCuenta())
             .append(" (").append(ctaOrigen.getCliente() != null ? ctaOrigen.getCliente().getNombreCompleto() : "ID " + ctaOrigen.getIdCliente()).append(")\n");

        if (esTransferencia) {
            if (itemDestino == null || itemDestino.getCuenta() == null) {
                mostrarAdvertencia("Debe seleccionar una cuenta destino.");
                return;
            }
            Cuenta ctaDestino = itemDestino.getCuenta();
            sbMsg.append("• Cuenta Destino: ").append(ctaDestino.getNumeroCuenta())
                 .append(" (").append(ctaDestino.getCliente() != null ? ctaDestino.getCliente().getNombreCompleto() : "ID " + ctaDestino.getIdCliente()).append(")\n");
        }

        sbMsg.append("• Monto a Operar: ").append(Formateador.aMoneda(monto)).append("\n");
        sbMsg.append("• Concepto: ").append(descripcion.isEmpty() ? "(Sin descripción adicional)" : descripcion).append("\n\n");
        sbMsg.append("Nota: La operación es atómica y afectará inmediatamente los balances contables.");

        int confirm = JOptionPane.showConfirmDialog(this, sbMsg.toString(),
                "Confirmación de Operación Financiera", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Transaccion txRealizada;
            if ("Depósito".equalsIgnoreCase(tipo)) {
                txRealizada = servicio.realizarDeposito(ctaOrigen.getIdCuenta(), monto, descripcion);
            } else if ("Retiro".equalsIgnoreCase(tipo)) {
                txRealizada = servicio.realizarRetiro(ctaOrigen.getIdCuenta(), monto, descripcion);
            } else {
                Cuenta ctaDestino = itemDestino.getCuenta();
                txRealizada = servicio.realizarTransferencia(ctaOrigen.getIdCuenta(), ctaDestino.getIdCuenta(), monto, descripcion);
            }

            JOptionPane.showMessageDialog(this,
                    "¡Transacción procesada con éxito!\n\n" +
                    "Código Generado: " + txRealizada.getCodigoTransaccion() + "\n" +
                    "Tipo: " + txRealizada.getTipo().getNombreVisual() + "\n" +
                    "Monto: " + Formateador.aMoneda(txRealizada.getMonto()) + "\n" +
                    "Saldo Resultante en Origen: " + Formateador.aMoneda(txRealizada.getSaldoResultante()),
                    "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormularioTransaccion();
            cargarCombosCuentas();
            cargarTablaTransacciones();
            cargarTablaCuentas();
            cargarEstadisticas();
            cargarMetricasReportes();
            generarReporteSeleccionado();

        } catch (BancoException ex) {
            mostrarError("Operación Rechazada", ex.getMessage());
        }
    }

    private void limpiarFormularioTransaccion() {
        txtTxMonto.setText("0.00");
        txtTxDescripcion.setText("");
        actualizarDetalleCuentasTx();
        validarFormularioTransaccion();
    }

    private void cargarTablaTransacciones() {
        try {
            modeloTablaTransacciones.setRowCount(0);
            List<Transaccion> lista = servicio.listarTransacciones();
            for (Transaccion tx : lista) {
                agregarFilaTransaccion(tx);
            }
        } catch (BancoException e) {
            mostrarError("Error al cargar historial", e.getMessage());
        }
    }

    private void agregarFilaTransaccion(Transaccion tx) {
        String numOrigen = tx.getNumeroCuentaOrigen() != null ? tx.getNumeroCuentaOrigen() : "-";
        String numDestino = tx.getNumeroCuentaDestino() != null ? tx.getNumeroCuentaDestino() : "-";

        modeloTablaTransacciones.addRow(new Object[]{
                tx.getIdTransaccion(),
                tx.getCodigoTransaccion(),
                tx.getTipo() != null ? tx.getTipo().getNombreVisual() : "-",
                numOrigen,
                numDestino,
                Formateador.aMoneda(tx.getMonto()),
                Formateador.aMoneda(tx.getSaldoResultante()),
                Formateador.aFecha(tx.getFechaHora()),
                tx.getDescripcion() != null ? tx.getDescripcion() : ""
        });
    }

    private void buscarTransaccion() {
        String termino = txtTxBuscar.getText().trim();
        if (termino.isEmpty()) {
            cargarTablaTransacciones();
            return;
        }

        try {
            modeloTablaTransacciones.setRowCount(0);
            Transaccion encontrada = null;

            // Búsqueda por ID si es número
            if (termino.matches("^\\d+$")) {
                encontrada = servicio.buscarTransaccionPorId(Integer.parseInt(termino));
            }
            // Búsqueda por Código exacto
            if (encontrada == null) {
                encontrada = servicio.buscarTransaccionPorCodigo(termino);
            }

            if (encontrada != null) {
                agregarFilaTransaccion(encontrada);
            } else {
                // Búsqueda amplia por coincidencia parcial en la lista
                List<Transaccion> todas = servicio.listarTransacciones();
                boolean hayCoincidencias = false;
                for (Transaccion tx : todas) {
                    if (tx.getCodigoTransaccion().toLowerCase().contains(termino.toLowerCase()) ||
                        (tx.getNumeroCuentaOrigen() != null && tx.getNumeroCuentaOrigen().toLowerCase().contains(termino.toLowerCase())) ||
                        (tx.getNumeroCuentaDestino() != null && tx.getNumeroCuentaDestino().toLowerCase().contains(termino.toLowerCase()))) {
                        agregarFilaTransaccion(tx);
                        hayCoincidencias = true;
                    }
                }
                if (!hayCoincidencias) {
                    mostrarAdvertencia("No se encontró ninguna transacción con el criterio: '" + termino + "'.");
                    cargarTablaTransacciones();
                }
            }
        } catch (BancoException e) {
            mostrarError("Error en búsqueda de transacciones", e.getMessage());
        }
    }

    // =========================================================================
    // LÓGICA DE CONTROL Y EVENTOS: REPORTES Y AUDITORÍA (Fase 2)
    // =========================================================================

    private void cargarMetricasReportes() {
        try {
            double depositado = servicio.obtenerTotalDepositado();
            double retirado = servicio.obtenerTotalRetirado();
            double transferido = servicio.obtenerTotalTransferido();
            Map<String, Object> stats = servicio.obtenerEstadisticasGlobales();
            double saldoCustodia = (double) stats.get("saldoTotalCustodia");
            int totalTx = (int) stats.get("totalTransacciones");

            lblRepTotalDepositado.setText(Formateador.aMoneda(depositado));
            lblRepTotalRetirado.setText(Formateador.aMoneda(retirado));
            lblRepTotalTransferido.setText(Formateador.aMoneda(transferido));
            lblRepSaldoCustodia.setText(Formateador.aMoneda(saldoCustodia));
            lblRepTotalTx.setText(String.valueOf(totalTx));

        } catch (BancoException e) {
            logger.error("No se pudieron calcular los totales contables del reporte: {}", e.getMessage(), e);
        }
    }

    private void generarReporteSeleccionado() {
        int opcion = cmbRepTipoReporte.getSelectedIndex();
        try {
            switch (opcion) {
                case 0: // Cuentas Bancarias Activas
                    List<Cuenta> activas = servicio.reporteCuentasActivas();
                    modeloTablaReportes.setDataVector(new Object[0][0], new String[]{
                            "ID", "N° Cuenta", "Tipo", "Cliente Titular", "Saldo ($)", "Rend. Mensual ($)", "Estado", "Fecha Apertura"
                    });
                    for (Cuenta c : activas) {
                        String titular = (c.getCliente() != null) ? c.getCliente().getNombreCompleto() : "ID: " + c.getIdCliente();
                        modeloTablaReportes.addRow(new Object[]{
                                c.getIdCuenta(),
                                c.getNumeroCuenta(),
                                c.getTipoCuenta().getNombreVisual(),
                                titular,
                                Formateador.aMoneda(c.getSaldo()),
                                Formateador.aMoneda(c.calcularRendimientoEstimadoMensual()),
                                c.getEstado(),
                                Formateador.aFecha(c.getFechaApertura())
                        });
                    }
                    lblRepContador.setText("Reporte: Cuentas Activas (" + activas.size() + " registros)");
                    break;

                case 1: // Cuentas Bancarias Bloqueadas
                    List<Cuenta> bloqueadas = servicio.reporteCuentasBloqueadas();
                    modeloTablaReportes.setDataVector(new Object[0][0], new String[]{
                            "ID", "N° Cuenta", "Tipo", "Cliente Titular", "Saldo ($)", "Rend. Mensual ($)", "Estado", "Fecha Apertura"
                    });
                    for (Cuenta c : bloqueadas) {
                        String titular = (c.getCliente() != null) ? c.getCliente().getNombreCompleto() : "ID: " + c.getIdCliente();
                        modeloTablaReportes.addRow(new Object[]{
                                c.getIdCuenta(),
                                c.getNumeroCuenta(),
                                c.getTipoCuenta().getNombreVisual(),
                                titular,
                                Formateador.aMoneda(c.getSaldo()),
                                Formateador.aMoneda(c.calcularRendimientoEstimadoMensual()),
                                c.getEstado(),
                                Formateador.aFecha(c.getFechaApertura())
                        });
                    }
                    lblRepContador.setText("Reporte: Cuentas Bloqueadas (" + bloqueadas.size() + " registros)");
                    break;

                case 2: // Clientes Registrados con Saldo Consolidado
                    List<Map<String, Object>> consolidado = servicio.reporteClientesRegistrados();
                    modeloTablaReportes.setDataVector(new Object[0][0], new String[]{
                            "ID", "Código", "DUI", "Nombre Completo", "Teléfono", "Correo", "Estado", "Cuentas Asociadas", "Saldo Consolidado ($)"
                    });
                    for (Map<String, Object> fila : consolidado) {
                        Cliente c = (Cliente) fila.get("cliente");
                        int cantCuentas = (int) fila.get("cantidadCuentas");
                        double saldoTotal = (double) fila.get("saldoTotal");
                        modeloTablaReportes.addRow(new Object[]{
                                c.getId(),
                                c.getCodigoCliente(),
                                c.getDui(),
                                c.getNombreCompleto(),
                                c.getTelefono(),
                                c.getCorreoElectronico(),
                                c.getEstado(),
                                cantCuentas,
                                Formateador.aMoneda(saldoTotal)
                        });
                    }
                    lblRepContador.setText("Reporte: Clientes con Saldo Consolidado (" + consolidado.size() + " clientes)");
                    break;

                case 3: // Historial General de Transacciones
                    List<Transaccion> txGeneral = servicio.reporteHistorialTransacciones();
                    modeloTablaReportes.setDataVector(new Object[0][0], new String[]{
                            "ID", "Código", "Tipo", "Cuenta Origen", "Cuenta Destino", "Monto ($)", "Saldo Result. ($)", "Fecha / Hora", "Descripción"
                    });
                    for (Transaccion tx : txGeneral) {
                        modeloTablaReportes.addRow(new Object[]{
                                tx.getIdTransaccion(),
                                tx.getCodigoTransaccion(),
                                tx.getTipo() != null ? tx.getTipo().getNombreVisual() : "-",
                                tx.getNumeroCuentaOrigen() != null ? tx.getNumeroCuentaOrigen() : "-",
                                tx.getNumeroCuentaDestino() != null ? tx.getNumeroCuentaDestino() : "-",
                                Formateador.aMoneda(tx.getMonto()),
                                Formateador.aMoneda(tx.getSaldoResultante()),
                                Formateador.aFecha(tx.getFechaHora()),
                                tx.getDescripcion() != null ? tx.getDescripcion() : ""
                        });
                    }
                    lblRepContador.setText("Reporte: Historial General (" + txGeneral.size() + " movimientos)");
                    break;

                case 4: // Transacciones por Cuenta Específica
                    CuentaItemCombo item = (CuentaItemCombo) cmbRepCuenta.getSelectedItem();
                    if (item == null || item.getCuenta() == null) {
                        mostrarAdvertencia("Seleccione una cuenta bancaria para visualizar sus movimientos.");
                        return;
                    }
                    int idCuenta = item.getCuenta().getIdCuenta();
                    List<Transaccion> txCuenta = servicio.reporteTransaccionesPorCuenta(idCuenta);
                    modeloTablaReportes.setDataVector(new Object[0][0], new String[]{
                            "ID", "Código", "Tipo", "Cuenta Origen", "Cuenta Destino", "Monto ($)", "Saldo Result. ($)", "Fecha / Hora", "Descripción"
                    });
                    for (Transaccion tx : txCuenta) {
                        modeloTablaReportes.addRow(new Object[]{
                                tx.getIdTransaccion(),
                                tx.getCodigoTransaccion(),
                                tx.getTipo() != null ? tx.getTipo().getNombreVisual() : "-",
                                tx.getNumeroCuentaOrigen() != null ? tx.getNumeroCuentaOrigen() : "-",
                                tx.getNumeroCuentaDestino() != null ? tx.getNumeroCuentaDestino() : "-",
                                Formateador.aMoneda(tx.getMonto()),
                                Formateador.aMoneda(tx.getSaldoResultante()),
                                Formateador.aFecha(tx.getFechaHora()),
                                tx.getDescripcion() != null ? tx.getDescripcion() : ""
                        });
                    }
                    lblRepContador.setText("Reporte: Movimientos de Cuenta " + item.getCuenta().getNumeroCuenta() + " (" + txCuenta.size() + " movimientos)");
                    break;
            }
            tblReportes.setRowHeight(24);
        } catch (BancoException ex) {
            mostrarError("Error al generar reporte", ex.getMessage());
        }
    }

    private void mostrarDialogoBitacora() {
        JDialog dlg = new JDialog(this, "Bitácora de Auditoría del Sistema (Apache Log4J 2)", true);
        dlg.setSize(920, 620);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout(8, 4));
        pnlHeader.setBorder(new EmptyBorder(12, 16, 6, 16));
        pnlHeader.setBackground(new Color(24, 43, 73));

        JLabel lblTitulo = new JLabel("AUDITORÍA DE EVENTOS Y TRANSACCIONES BANCARIAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblRuta = new JLabel("Cargando registro físico de bitácora...");
        lblRuta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRuta.setForeground(new Color(200, 215, 235));

        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblRuta, BorderLayout.SOUTH);

        JTextArea txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtLog.setBackground(new Color(20, 24, 30));
        txtLog.setForeground(new Color(220, 230, 242));
        txtLog.setCaretColor(Color.WHITE);
        txtLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane spLog = new JScrollPane(txtLog);
        spLog.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 75)));

        Runnable recargarLog = () -> {
            File archivo = new File("logs/banco.log");
            if (!archivo.exists()) {
                File alt = new File("Experienciadeaprendizaje3/logs/banco.log");
                if (alt.exists()) {
                    archivo = alt;
                }
            }
            if (!archivo.exists()) {
                txtLog.setText("No se encontró el archivo de log en la ruta: " + archivo.getAbsolutePath() +
                        "\n\nEl archivo se genera automáticamente al iniciar el servicio bancario o ejecutar operaciones.");
                lblRuta.setText("Archivo físico no detectado en: " + archivo.getPath());
                return;
            }
            try {
                List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
                int total = lineas.size();
                int maxLineas = 400;
                int desde = Math.max(0, total - maxLineas);
                StringBuilder sb = new StringBuilder();
                sb.append("=== BITÁCORA BANCO ACADÉMICO UDB — Mostrando últimas ")
                  .append(total - desde).append(" de ").append(total).append(" líneas registradas ===\n\n");
                for (int i = desde; i < total; i++) {
                    sb.append(lineas.get(i)).append("\n");
                }
                txtLog.setText(sb.toString());
                txtLog.setCaretPosition(txtLog.getDocument().getLength());
                lblRuta.setText("Ruta: " + archivo.getAbsolutePath() + " (" + total + " líneas registradas, " + archivo.length() + " bytes)");
            } catch (Exception ex) {
                txtLog.setText("Error al leer bitácora: " + ex.getMessage());
                lblRuta.setText("Error de lectura en archivo de log.");
            }
        };

        recargarLog.run();

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnRefrescar = new JButton("🔄 Refrescar Bitácora");
        btnRefrescar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefrescar.addActionListener(e -> recargarLog.run());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCerrar.addActionListener(e -> dlg.dispose());

        pnlFooter.add(btnRefrescar);
        pnlFooter.add(btnCerrar);

        dlg.add(pnlHeader, BorderLayout.NORTH);
        dlg.add(spLog, BorderLayout.CENTER);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // =========================================================================
    // ESTADÍSTICAS GLOBALES Y UTILIDADES
    // =========================================================================

    private void cargarEstadisticas() {
        try {
            Map<String, Object> stats = servicio.obtenerEstadisticasGlobales();
            lblTotalClientes.setText(stats.get("totalClientes").toString());
            lblTotalCuentas.setText(stats.get("totalCuentas").toString());
            lblCuentasActivas.setText(stats.get("cuentasActivas").toString());
            lblCuentasBloqueadas.setText(stats.get("cuentasBloqueadas").toString());
            lblSaldoGlobal.setText(Formateador.aMoneda((double) stats.get("saldoTotalCustodia")));

            lblDashTotalDepositado.setText(Formateador.aMoneda((double) stats.get("totalDepositado")));
            lblDashTotalRetirado.setText(Formateador.aMoneda((double) stats.get("totalRetirado")));
            lblDashTotalTransferido.setText(Formateador.aMoneda((double) stats.get("totalTransferido")));
            lblDashTotalTx.setText(stats.get("totalTransacciones").toString());
        } catch (BancoException e) {
            logger.error("No se pudieron cargar las métricas del dashboard: {}", e.getMessage(), e);
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    // =========================================================================
    // CLASES AUXILIARES PARA COMBOBOXES DE DOMINIO
    // =========================================================================

    /**
     * Representa un elemento en el ComboBox de selección de Clientes.
     */
    private static class ClienteItemCombo {
        private final Cliente cliente;

        public ClienteItemCombo(Cliente cliente) {
            this.cliente = cliente;
        }

        public Cliente getCliente() {
            return cliente;
        }

        @Override
        public String toString() {
            return cliente.getCodigoCliente() + " - " + cliente.getNombreCompleto() + " (" + cliente.getDui() + ")";
        }
    }

    /**
     * Representa un elemento en el ComboBox de selección de Cuentas Bancarias.
     */
    private static class CuentaItemCombo {
        private final Cuenta cuenta;

        public CuentaItemCombo(Cuenta cuenta) {
            this.cuenta = cuenta;
        }

        public Cuenta getCuenta() {
            return cuenta;
        }

        @Override
        public String toString() {
            if (cuenta == null) return "-- Seleccione una cuenta --";
            String titular = (cuenta.getCliente() != null)
                    ? cuenta.getCliente().getNombreCompleto()
                    : "Cliente #" + cuenta.getIdCliente();
            return cuenta.getNumeroCuenta() + " - " + titular + " (" + Formateador.aMoneda(cuenta.getSaldo()) + ") [" + cuenta.getEstado() + "]";
        }
    }
}
