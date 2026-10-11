package banco.datos;

import banco.excepciones.BancoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexión JDBC con soporte transparente para:
 * 1. Docker (puerto 3308, user: banco_user/root, pass: banco_pass/root)
 * 2. XAMPP / MySQL Local (puerto 3306, user: root, pass: vacía)
 */
public class ConexionBD {

    public enum Perfil {
        DOCKER("Docker MySQL", "localhost", 3308, "banco_academico_db", "banco_user", "banco_pass"),
        XAMPP_LOCAL("XAMPP / MySQL Local", "localhost", 3306, "banco_academico_db", "root", "");

        private final String nombre;
        private final String host;
        private final int puerto;
        private final String baseDatos;
        private final String usuario;
        private final String password;

        Perfil(String nombre, String host, int puerto, String baseDatos, String usuario, String password) {
            this.nombre = nombre;
            this.host = host;
            this.puerto = puerto;
            this.baseDatos = baseDatos;
            this.usuario = usuario;
            this.password = password;
        }

        public String getNombre() { return nombre; }
        public String getHost() { return host; }
        public int getPuerto() { return puerto; }
        public String getBaseDatos() { return baseDatos; }
        public String getUsuario() { return usuario; }
        public String getPassword() { return password; }
    }

    private static Perfil perfilActivo = Perfil.DOCKER;
    private static String hostPersonalizado = null;
    private static Integer puertoPersonalizado = null;
    private static String usuarioPersonalizado = null;
    private static String passwordPersonalizado = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ADVERTENCIA: Driver MySQL JDBC no encontrado en classpath: " + e.getMessage());
        }
    }

    private ConexionBD() {}

    public static synchronized void setPerfil(Perfil perfil) {
        perfilActivo = perfil;
        hostPersonalizado = null;
        puertoPersonalizado = null;
        usuarioPersonalizado = null;
        passwordPersonalizado = null;
    }

    public static synchronized void configurarManual(String host, int puerto, String user, String pass) {
        hostPersonalizado = host;
        puertoPersonalizado = puerto;
        usuarioPersonalizado = user;
        passwordPersonalizado = pass;
    }

    public static Perfil getPerfilActivo() {
        return perfilActivo;
    }

    public static String getUrlConexion() {
        String host = (hostPersonalizado != null) ? hostPersonalizado : perfilActivo.getHost();
        int puerto = (puertoPersonalizado != null) ? puertoPersonalizado : perfilActivo.getPuerto();
        String bd = perfilActivo.getBaseDatos();
        return "jdbc:mysql://" + host + ":" + puerto + "/" + bd + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public static String getUsuarioActual() {
        return (usuarioPersonalizado != null) ? usuarioPersonalizado : perfilActivo.getUsuario();
    }

    public static String getPasswordActual() {
        return (passwordPersonalizado != null) ? passwordPersonalizado : perfilActivo.getPassword();
    }

    public static Connection obtenerConexion() throws BancoException {
        // Intento 1: Con la configuración activa actual
        try {
            return DriverManager.getConnection(getUrlConexion(), getUsuarioActual(), getPasswordActual());
        } catch (SQLException exOriginal) {
            // Si el perfil activo es Docker y falló, intentar fallback a XAMPP (o viceversa) si no hay config manual
            if (hostPersonalizado == null && puertoPersonalizado == null) {
                Perfil alternativo = (perfilActivo == Perfil.DOCKER) ? Perfil.XAMPP_LOCAL : Perfil.DOCKER;
                try {
                    String urlAlt = "jdbc:mysql://" + alternativo.getHost() + ":" + alternativo.getPuerto() + "/" + alternativo.getBaseDatos() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
                    Connection cAlt = DriverManager.getConnection(urlAlt, alternativo.getUsuario(), alternativo.getPassword());
                    perfilActivo = alternativo; // Auto-conmutación exitosa
                    return cAlt;
                } catch (SQLException exFallback) {
                    // Si ambos fallaron, lanzamos el error original con sugerencia
                    throw new BancoException("No se pudo conectar a la base de datos MySQL en " + perfilActivo.getNombre() + " (" + getUrlConexion() + ") ni en " + alternativo.getNombre() + ".\nVerifique que Docker o XAMPP esté iniciado y que el script '01_esquema_banco.sql' haya sido importado.\nDetalle del error: " + exOriginal.getMessage(), exOriginal);
                }
            }
            throw new BancoException("Error de conexión a la base de datos (" + getUrlConexion() + "): " + exOriginal.getMessage(), exOriginal);
        }
    }

    public static boolean probarConexion() {
        try (Connection conn = obtenerConexion()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void cerrar(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static void cerrar(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
    }

    public static void cerrar(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
    }

    public static void cerrar(Connection conn, Statement stmt, ResultSet rs) {
        cerrar(rs);
        cerrar(stmt);
        cerrar(conn);
    }
}
