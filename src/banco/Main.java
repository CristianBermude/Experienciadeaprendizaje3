package banco;

import banco.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada principal para el Sistema de Gestión Bancaria Académica (Banco Académico UDB).
 * Experiencia de Aprendizaje 3 — Fase 1.
 */
public class Main {

    public static void main(String[] args) {
        // Configurar Look & Feel moderno
        configurarAparienciaVisual();

        // Lanzar interfaz en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error crítico al iniciar la aplicación Swing: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void configurarAparienciaVisual() {
        try {
            // Intentar aplicar Look & Feel del Sistema Operativo
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                // Fallback a Nimbus si está disponible
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Se mantiene el Look & Feel básico de Java
            }
        }
    }
}
