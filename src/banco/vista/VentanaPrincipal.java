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
import banco.servicio.BancoServicio;
import banco.util.Formateador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * Ventana Principal del Sistema Bancario Académico UDB.
 * Implementa una interfaz de usuario Swing moderna, ergonómica y amigable.
 */
public class VentanaPrincipal extends JFrame {

    private final BancoServicio servicio;

    // Componentes del Dashboard
    private JLabel lblTotalClientes;
    private JLabel lblTotalCuentas;
    private JLabel lblCuentasActivas;
    private JLabel lblCuentasBloqueadas;
    private JLabel lblSaldoGlobal;
    private JLabel lblEstadoBD;

    // Componentes Módulo Clientes
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

    // Componentes Módulo Cuentas
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
    private JButton btnCtaLimpiar;


    public VentanaPrincipal() {
        this.servicio = new BancoServicio();
        inicializarVentana();
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarVentana() {
        setTitle("Banco Académico UDB — Sistema de Gestión Bancaria (POO901)");
        setSize(1150, 750);
        setMinimumSize(new Dimension(980, 650));
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

        JLabel lblSubtitulo = new JLabel("Ciclo 02/2026 · Proyecto Cátedra POO901 — Fase 1: Clientes y Cuentas Bancarias");
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

        JLabel lblInfo = new JLabel("Plataforma Bancaria Académica · Preparado para Fases 2 y 3");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        pnl.add(lblEstadoBD, BorderLayout.WEST);
        pnl.add(lblInfo, BorderLayout.EAST);
        return pnl;
    }

    // =========================================================================
    // PESTAÑA 1: DASHBOARD / RESUMEN
    // =========================================================================

    private JPanel crearPanelDashboard() {
        JPanel pnl = new JPanel(new BorderLayout(15, 15));
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlTarjetas = new JPanel(new GridLayout(1, 5, 12, 12));

        lblTotalClientes = new JLabel("0", SwingConstants.CENTER);
        lblTotalCuentas = new JLabel("0", SwingConstants.CENTER);
        lblCuentasActivas = new JLabel("0", SwingConstants.CENTER);
        lblCuentasBloqueadas = new JLabel("0", SwingConstants.CENTER);
        lblSaldoGlobal = new JLabel("$0.00", SwingConstants.CENTER);

        pnlTarjetas.add(crearTarjetaMetrica("Clientes Totales", lblTotalClientes, new Color(41, 128, 185)));
        pnlTarjetas.add(crearTarjetaMetrica("Cuentas Totales", lblTotalCuentas, new Color(52, 73, 94)));
        pnlTarjetas.add(crearTarjetaMetrica("Cuentas Activas", lblCuentasActivas, new Color(39, 174, 96)));
        pnlTarjetas.add(crearTarjetaMetrica("Cuentas Bloqueadas", lblCuentasBloqueadas, new Color(192, 57, 43)));
        pnlTarjetas.add(crearTarjetaMetrica("Saldo en Custodia", lblSaldoGlobal, new Color(142, 68, 173)));

        pnl.add(pnlTarjetas, BorderLayout.NORTH);

        // Panel Informativo Central
        JPanel pnlDetalle = new JPanel(new BorderLayout(10, 10));
        pnlDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Alcance del Sistema Bancario UDB",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));

        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setText(
                "BIENVENIDO AL SISTEMA DE GESTIÓN BANCARIA ACADÉMICA (BANCO ACADÉMICO UDB)\n\n" +
                "• FASE 1 (Actual): Módulos de Clientes y Cuentas Bancarias con persistencia en MySQL y JDBC.\n" +
                "  - CRUD completo de clientes con validaciones de DUI salvadoreño, correo, teléfono y estados.\n" +
                "  - Creación de cuentas bancarias asociadas a clientes, con tipos Ahorro y Corriente.\n" +
                "  - Arquitectura desacoplada en capas (Modelo, Datos, Servicio, Vista, Utilidades).\n\n" +
                "• FASE 2 (Proyectada): Sistema Desktop Completo con Transacciones, Reportes y Log4J.\n" +
                "  - Depósitos, retiros con validación de saldo suficiente y transferencias entre cuentas activas.\n" +
                "  - Auditoría de bitácora mediante Log4J en archivo de logs físico.\n\n" +
                "• FASE 3 (Proyectada): Migración a Plataforma Web con HTML, Servlets, JSP y JavaBeans.\n" +
                "  - Reutilización directa del servicio de negocio y modelos JavaBeans sobre Apache Tomcat.\n" +
                "  - Roles mínimos: Administrador, Cajero y Cliente."
        );
        pnlDetalle.add(new JScrollPane(txtInfo), BorderLayout.CENTER);

        JButton btnRefrescarDashboard = new JButton("🔄 Actualizar Métricas");
        btnRefrescarDashboard.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefrescarDashboard.addActionListener(e -> cargarEstadisticas());
        pnlDetalle.add(btnRefrescarDashboard, BorderLayout.SOUTH);

        pnl.add(pnlDetalle, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel lblValor, Color colorFondo) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(colorFondo);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTit.setForeground(new Color(240, 240, 240));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
        JButton btnGenerarNum = new JButton("🎲 Generar N°");
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
    // LÓGICA DE CONTROL Y EVENTOS: CLIENTES
    // =========================================================================

    private void cargarDatosIniciales() {
        actualizarEstadoConexion();
        cargarTablaClientes();
        cargarComboClientes();
        cargarTablaCuentas();
        cargarEstadisticas();
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
            // Ignorar en inicio si no hay conexión aún
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
            cargarEstadisticas();
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
            cargarEstadisticas();
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
            cargarEstadisticas();
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
            
            // Cargar tipo de cuenta
            String tipoVisual = modeloTablaCuentas.getValueAt(fila, 2).toString();
            if (tipoVisual.contains("Ahorro")) {
                cmbCtaTipo.setSelectedItem(TipoCuenta.AHORRO);
            } else {
                cmbCtaTipo.setSelectedItem(TipoCuenta.CORRIENTE);
            }

            // Cargar saldo actual y proteger contra edición manual
            String saldoActual = modeloTablaCuentas.getValueAt(fila, 4).toString();
            txtCtaSaldo.setText(saldoActual.replace("$", "").replace(",", "").trim());
            txtCtaSaldo.setEditable(false);
            txtCtaSaldo.setToolTipText("El saldo actual no se puede modificar manualmente. Solo cambia mediante transacciones bancarias.");

            EstadoCuenta est = (EstadoCuenta) modeloTablaCuentas.getValueAt(fila, 6);
            cmbCtaEstado.setSelectedItem(est);

            btnCtaAperturar.setEnabled(false);
            btnCtaCambiarEstado.setEnabled(true);
        }
    }

    private void limpiarFormularioCuenta() {
        txtCtaId.setText("");
        txtCtaNumero.setText("");
        txtCtaSaldo.setText("0.00");
        txtCtaSaldo.setEditable(true);
        txtCtaSaldo.setToolTipText("Monto con el que se abre la cuenta bancaria");
        cmbCtaTipo.setSelectedItem(TipoCuenta.AHORRO);
        cmbCtaEstado.setSelectedItem(EstadoCuenta.ACTIVA);

        tblCuentas.clearSelection();
        btnCtaAperturar.setEnabled(true);
        btnCtaCambiarEstado.setEnabled(false);
    }

    // =========================================================================
    // ESTADÍSTICAS Y UTILIDADES DE DIÁLOGOS
    // =========================================================================

    private void cargarEstadisticas() {
        try {
            Map<String, Object> stats = servicio.obtenerEstadisticasGlobales();
            lblTotalClientes.setText(stats.get("totalClientes").toString());
            lblTotalCuentas.setText(stats.get("totalCuentas").toString());
            lblCuentasActivas.setText(stats.get("cuentasActivas").toString());
            lblCuentasBloqueadas.setText(stats.get("cuentasBloqueadas").toString());
            lblSaldoGlobal.setText(Formateador.aMoneda((double) stats.get("saldoTotalCustodia")));
        } catch (BancoException e) {
            // Ignorar en caso de BD no lista
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    // Clase auxiliar para el ComboBox de Selección de Cliente Titular
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
}
