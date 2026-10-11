package banco;

import banco.datos.ConexionBD;
import banco.modelo.Cliente;
import banco.modelo.Cuenta;
import banco.servicio.BancoServicio;
import java.util.List;
import java.util.Map;

public class TestIntegracion {
    public static void main(String[] args) {
        System.out.println("=== TEST DE INTEGRACIÓN: BANCO ACADÉMICO UDB ===");
        try {
            boolean conecto = ConexionBD.probarConexion();
            System.out.println("1. Prueba de Conexión JDBC: " + (conecto ? "EXITOSA ✓" : "FALLIDA ✗"));
            if (!conecto) {
                System.err.println("No se pudo conectar.");
                System.exit(1);
            }

            BancoServicio servicio = new BancoServicio();
            List<Cliente> clientes = servicio.listarClientes();
            System.out.println("2. Total de Clientes en BD: " + clientes.size() + " ✓");
            for (Cliente c : clientes) {
                System.out.println("   - [" + c.getCodigoCliente() + "] " + c.getNombreCompleto() + " (" + c.getDui() + ") - " + c.getEstado());
            }

            List<Cuenta> cuentas = servicio.listarCuentas();
            System.out.println("3. Total de Cuentas en BD: " + cuentas.size() + " ✓");
            for (Cuenta c : cuentas) {
                System.out.println("   - " + c.getNumeroCuenta() + " [" + c.getTipoCuenta() + "] Saldo: $" + c.getSaldo() + " (Rendimiento mensual polimórfico: $" + String.format("%.2f", c.calcularRendimientoEstimadoMensual()) + ")");
            }

            Map<String, Object> stats = servicio.obtenerEstadisticasGlobales();
            System.out.println("4. Estadísticas del Dashboard: " + stats + " ✓");

            System.out.println("=== TODAS LAS PRUEBAS DE INTEGRACIÓN PASARON EXITOSAMENTE ===");
        } catch (Exception e) {
            System.err.println("Error en test de integración: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
