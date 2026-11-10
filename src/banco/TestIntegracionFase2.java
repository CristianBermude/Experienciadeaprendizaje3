package banco;

import banco.datos.ConexionBD;
import banco.excepciones.BancoException;
import banco.excepciones.CuentaBloqueadaException;
import banco.excepciones.CuentaNoDisponibleException;
import banco.excepciones.FondosInsuficientesException;
import banco.excepciones.TransaccionInvalidaException;
import banco.modelo.Cliente;
import banco.modelo.Cuenta;
import banco.modelo.EstadoCuenta;
import banco.modelo.Transaccion;
import banco.servicio.BancoServicio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Suite de Pruebas de Integración Automatizadas - Fase 2
 * Sistema de Gestión Bancaria Académica (Banco Académico UDB - POO901)
 *
 * Valida de forma exhaustiva:
 * 1. Conectividad JDBC y estado inicial del sistema.
 * 2. Operaciones de Depósito (éxito, validación de montos <= 0 y cuenta bloqueada).
 * 3. Operaciones de Retiro (éxito, validación de fondos insuficientes y cuenta bloqueada).
 * 4. Transferencias atómicas ACID (descuento en origen, abono en destino).
 * 5. Rollback transaccional (fondos insuficientes, cuenta destino inexistente/bloqueada, cuentas idénticas).
 * 6. Reportes y consultas consolidadas (cuentas activas, bloqueadas, resumen de clientes, totales contables).
 * 7. Generación física de bitácora y persistencia de eventos INFO/WARN/ERROR en logs/banco.log mediante Log4J 2.
 */
public class TestIntegracionFase2 {

    private static int totalPruebas = 0;
    private static int pruebasAprobadas = 0;
    private static int pruebasFallidas = 0;

    private static final double EPSILON = 0.001;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  BANCO ACADÉMICO UDB - SUITE DE INTEGRACIÓN AUTOMATIZADA (FASE 2)");
        System.out.println("  Asignatura: Programación Orientada a Objetos (POO901)");
        System.out.println("================================================================================\n");

        long inicio = System.currentTimeMillis();

        try {
            BancoServicio servicio = new BancoServicio();

            // Módulo 1: Conectividad y Verificación de Entorno
            testConectividad();

            // Módulo 2: Operaciones de Depósito
            testDepositos(servicio);

            // Módulo 3: Operaciones de Retiro
            testRetiros(servicio);

            // Módulo 4: Transferencias Atómicas ACID y Rollback
            testTransferencias(servicio);

            // Módulo 5: Reportes y Totales Contables
            testReportesYTotales(servicio);

            // Módulo 6: Auditoría y Bitácora con Log4J 2
            testLog4j2();

        } catch (Throwable t) {
            System.err.println("\n[FATAL] Error catastrófico no recuperable durante la ejecución de la suite:");
            t.printStackTrace();
            pruebasFallidas++;
        }

        long tiempoMs = System.currentTimeMillis() - inicio;

        System.out.println("\n================================================================================");
        System.out.println("  RESUMEN DE RESULTADOS DE PRUEBAS DE INTEGRACIÓN (FASE 2)");
        System.out.println("================================================================================");
        System.out.println(String.format("  Total de Casos Evaluados : %d", totalPruebas));
        System.out.println(String.format("  Casos Aprobados (PASS)   : %d (%.1f%%)", pruebasAprobadas,
                totalPruebas > 0 ? (pruebasAprobadas * 100.0 / totalPruebas) : 0));
        System.out.println(String.format("  Casos Fallidos  (FAIL)   : %d", pruebasFallidas));
        System.out.println(String.format("  Tiempo Total de Ejecución: %d ms", tiempoMs));
        System.out.println("================================================================================");

        if (pruebasFallidas == 0 && totalPruebas > 0) {
            System.out.println("  VEREDICTO FINAL: >>> 100% EXCELENTE - TODAS LAS ASIGNACIONES PASARON <<<");
            System.out.println("================================================================================\n");
            System.exit(0);
        } else {
            System.err.println("  VEREDICTO FINAL: >>> PRUEBAS NO SUPERADAS AL 100% <<<");
            System.out.println("================================================================================\n");
            System.exit(1);
        }
    }

    // =========================================================================
    // MÓDULO 1: CONECTIVIDAD
    // =========================================================================

    private static void testConectividad() {
        System.out.println("--- [MODULO 01: CONECTIVIDAD Y BASE DE DATOS] ---");
        try {
            boolean conecto = ConexionBD.probarConexion();
            afirmar("CP-BD-01: Verificación de Conexión JDBC (Docker/XAMPP)", conecto);
        } catch (Exception e) {
            afirmar("CP-BD-01: Verificación de Conexión JDBC (Docker/XAMPP)", false, e.getMessage());
        }
    }

    // =========================================================================
    // MÓDULO 2: DEPÓSITOS BANCARIOS
    // =========================================================================

    private static void testDepositos(BancoServicio servicio) throws Exception {
        System.out.println("\n--- [MODULO 02: DEPÓSITOS BANCARIOS] ---");

        // Cuenta activa para pruebas: CTA-2026-00001 (ID 1)
        Cuenta ctaActiva = servicio.buscarCuentaPorId(1);
        double saldoInicial = ctaActiva.getSaldo();
        double montoDeposito = 125.50;

        // CP-TX-01: Depósito Exitoso
        try {
            Transaccion tx = servicio.realizarDeposito(1, montoDeposito, "Depósito de prueba automatizada");
            Cuenta ctaActualizada = servicio.buscarCuentaPorId(1);
            double saldoEsperado = saldoInicial + montoDeposito;

            boolean saldoCorrecto = Math.abs(ctaActualizada.getSaldo() - saldoEsperado) < EPSILON;
            boolean txCorrecta = tx != null
                    && tx.getTipo() == Transaccion.Tipo.DEPOSITO
                    && tx.getCodigoTransaccion().startsWith("TX-DEP-")
                    && Math.abs(tx.getMonto() - montoDeposito) < EPSILON
                    && Math.abs(tx.getSaldoResultante() - saldoEsperado) < EPSILON;

            afirmar("CP-TX-01: Depósito válido incrementa saldo y genera transacción en BD", saldoCorrecto && txCorrecta,
                    String.format("Saldo anterior: $%.2f, nuevo: $%.2f, esperado: $%.2f",
                            saldoInicial, ctaActualizada.getSaldo(), saldoEsperado));
        } catch (Exception e) {
            afirmar("CP-TX-01: Depósito válido incrementa saldo y genera transacción en BD", false, e.getMessage());
        }

        // CP-TX-02: Rechazo ante Monto Inválido (Cero o Negativo)
        double saldoAntesInvalido = servicio.buscarCuentaPorId(1).getSaldo();
        boolean lanzoExcepcionMontoCero = false;
        try {
            servicio.realizarDeposito(1, 0.0, "Depósito con monto cero");
        } catch (TransaccionInvalidaException e) {
            lanzoExcepcionMontoCero = true;
        } catch (BancoException e) {
            lanzoExcepcionMontoCero = true;
        }

        boolean lanzoExcepcionMontoNegativo = false;
        try {
            servicio.realizarDeposito(1, -50.0, "Depósito con monto negativo");
        } catch (TransaccionInvalidaException e) {
            lanzoExcepcionMontoNegativo = true;
        } catch (BancoException e) {
            lanzoExcepcionMontoNegativo = true;
        }

        double saldoDespuesInvalido = servicio.buscarCuentaPorId(1).getSaldo();
        boolean saldoIntacto = Math.abs(saldoAntesInvalido - saldoDespuesInvalido) < EPSILON;

        afirmar("CP-TX-02: Rechazo ante depósito con monto <= 0 y preservación de saldo",
                lanzoExcepcionMontoCero && lanzoExcepcionMontoNegativo && saldoIntacto,
                String.format("Cero=%b, Negativo=%b, SaldoIntacto=%b",
                        lanzoExcepcionMontoCero, lanzoExcepcionMontoNegativo, saldoIntacto));

        // CP-TX-03: Rechazo ante Cuenta Bloqueada
        // CTA-2026-00005 (ID 5) está BLOQUEADA
        Cuenta ctaBloqueada = servicio.buscarCuentaPorId(5);
        double saldoBloqueadaAntes = ctaBloqueada.getSaldo();
        boolean lanzoExcepcionBloqueada = false;

        try {
            servicio.realizarDeposito(5, 100.0, "Depósito a cuenta bloqueada");
        } catch (CuentaBloqueadaException e) {
            lanzoExcepcionBloqueada = true;
        } catch (BancoException e) {
            lanzoExcepcionBloqueada = true;
        }

        double saldoBloqueadaDespues = servicio.buscarCuentaPorId(5).getSaldo();
        boolean saldoBloqueadaIntacto = Math.abs(saldoBloqueadaAntes - saldoBloqueadaDespues) < EPSILON;

        afirmar("CP-TX-03: Rechazo ante depósito en cuenta BLOQUEADA (CuentaBloqueadaException)",
                lanzoExcepcionBloqueada && saldoBloqueadaIntacto,
                String.format("Excepcion=%b, SaldoIntacto=%b", lanzoExcepcionBloqueada, saldoBloqueadaIntacto));
    }

    // =========================================================================
    // MÓDULO 3: RETIROS BANCARIOS
    // =========================================================================

    private static void testRetiros(BancoServicio servicio) throws Exception {
        System.out.println("\n--- [MODULO 03: RETIROS BANCARIOS] ---");

        // Cuenta activa: CTA-2026-00002 (ID 2)
        Cuenta cta = servicio.buscarCuentaPorId(2);
        double saldoInicial = cta.getSaldo();
        double montoRetiro = 75.00;

        // CP-TX-04: Retiro Exitoso con Fondos Suficientes
        try {
            Transaccion tx = servicio.realizarRetiro(2, montoRetiro, "Retiro de prueba automatizada");
            Cuenta ctaActualizada = servicio.buscarCuentaPorId(2);
            double saldoEsperado = saldoInicial - montoRetiro;

            boolean saldoCorrecto = Math.abs(ctaActualizada.getSaldo() - saldoEsperado) < EPSILON;
            boolean txCorrecta = tx != null
                    && tx.getTipo() == Transaccion.Tipo.RETIRO
                    && tx.getCodigoTransaccion().startsWith("TX-RET-")
                    && Math.abs(tx.getMonto() - montoRetiro) < EPSILON
                    && Math.abs(tx.getSaldoResultante() - saldoEsperado) < EPSILON;

            afirmar("CP-TX-04: Retiro válido deduce saldo y genera transacción en BD", saldoCorrecto && txCorrecta,
                    String.format("Saldo anterior: $%.2f, nuevo: $%.2f, esperado: $%.2f",
                            saldoInicial, ctaActualizada.getSaldo(), saldoEsperado));
        } catch (Exception e) {
            afirmar("CP-TX-04: Retiro válido deduce saldo y genera transacción en BD", false, e.getMessage());
        }

        // CP-TX-05: Rechazo ante Fondos Insuficientes (FondosInsuficientesException)
        Cuenta ctaParaRetiro = servicio.buscarCuentaPorId(2);
        double saldoActual = ctaParaRetiro.getSaldo();
        double montoExcesivo = saldoActual + 5000.00;
        boolean lanzoFondosInsuficientes = false;
        FondosInsuficientesException excepcionCapturada = null;

        try {
            servicio.realizarRetiro(2, montoExcesivo, "Intento de sobregiro");
        } catch (FondosInsuficientesException e) {
            lanzoFondosInsuficientes = true;
            excepcionCapturada = e;
        } catch (BancoException e) {
            if (e instanceof FondosInsuficientesException) {
                lanzoFondosInsuficientes = true;
                excepcionCapturada = (FondosInsuficientesException) e;
            }
        }

        double saldoDespuesSobreGiro = servicio.buscarCuentaPorId(2).getSaldo();
        boolean saldoNoVariado = Math.abs(saldoActual - saldoDespuesSobreGiro) < EPSILON;
        boolean detalleExcepcionValido = excepcionCapturada != null
                && Math.abs(excepcionCapturada.getSaldoDisponible() - saldoActual) < EPSILON
                && Math.abs(excepcionCapturada.getMontoRequerido() - montoExcesivo) < EPSILON;

        afirmar("CP-TX-05: Rechazo por fondos insuficientes (FondosInsuficientesException) con saldo intacto",
                lanzoFondosInsuficientes && saldoNoVariado && detalleExcepcionValido,
                String.format("Excepcion=%b, SaldoIntacto=%b, InfoExcepcion=%b",
                        lanzoFondosInsuficientes, saldoNoVariado, detalleExcepcionValido));

        // CP-TX-06: Rechazo ante Retiro en Cuenta Bloqueada
        double saldoBloqAntes = servicio.buscarCuentaPorId(5).getSaldo();
        boolean lanzoBloqueadaRetiro = false;
        try {
            servicio.realizarRetiro(5, 50.0, "Retiro en cuenta bloqueada");
        } catch (CuentaBloqueadaException e) {
            lanzoBloqueadaRetiro = true;
        } catch (BancoException e) {
            lanzoBloqueadaRetiro = true;
        }

        double saldoBloqDespues = servicio.buscarCuentaPorId(5).getSaldo();
        boolean saldoBloqIntacto = Math.abs(saldoBloqAntes - saldoBloqDespues) < EPSILON;

        afirmar("CP-TX-06: Rechazo ante retiro en cuenta BLOQUEADA con saldo inalterado",
                lanzoBloqueadaRetiro && saldoBloqIntacto,
                String.format("Excepcion=%b, SaldoIntacto=%b", lanzoBloqueadaRetiro, saldoBloqIntacto));
    }

    // =========================================================================
    // MÓDULO 4: TRANSFERENCIAS ATÓMICAS ACID Y ROLLBACK
    // =========================================================================

    private static void testTransferencias(BancoServicio servicio) throws Exception {
        System.out.println("\n--- [MODULO 04: TRANSFERENCIAS ATÓMICAS ACID Y ROLLBACK] ---");

        // Usamos Cuenta 3 (CTA-2026-00003) y Cuenta 4 (CTA-2026-00004)
        Cuenta ctaOrigen = servicio.buscarCuentaPorId(3);
        Cuenta ctaDestino = servicio.buscarCuentaPorId(4);

        double saldoOrigenInicial = ctaOrigen.getSaldo();
        double saldoDestinoInicial = ctaDestino.getSaldo();
        double montoTransferencia = 100.00;

        // CP-TX-07: Transferencia Atómica Exitosa
        try {
            Transaccion tx = servicio.realizarTransferencia(3, 4, montoTransferencia, "Transferencia atómica UDB");

            Cuenta ctaOrigenPost = servicio.buscarCuentaPorId(3);
            Cuenta ctaDestinoPost = servicio.buscarCuentaPorId(4);

            double saldoOrigenEsperado = saldoOrigenInicial - montoTransferencia;
            double saldoDestinoEsperado = saldoDestinoInicial + montoTransferencia;

            boolean origenCorrecto = Math.abs(ctaOrigenPost.getSaldo() - saldoOrigenEsperado) < EPSILON;
            boolean destinoCorrecto = Math.abs(ctaDestinoPost.getSaldo() - saldoDestinoEsperado) < EPSILON;

            boolean txCorrecta = tx != null
                    && tx.getTipo() == Transaccion.Tipo.TRANSFERENCIA
                    && tx.getCodigoTransaccion().startsWith("TX-TRF-")
                    && tx.getIdCuentaOrigen() == 3
                    && tx.getIdCuentaDestino() == 4
                    && Math.abs(tx.getMonto() - montoTransferencia) < EPSILON
                    && Math.abs(tx.getSaldoResultante() - saldoOrigenEsperado) < EPSILON;

            afirmar("CP-TX-07: Transferencia atómica ACID exitosa (débito origen y crédito destino simultáneo)",
                    origenCorrecto && destinoCorrecto && txCorrecta,
                    String.format("Origen: $%.2f -> $%.2f, Destino: $%.2f -> $%.2f",
                            saldoOrigenInicial, ctaOrigenPost.getSaldo(),
                            saldoDestinoInicial, ctaDestinoPost.getSaldo()));
        } catch (Exception e) {
            afirmar("CP-TX-07: Transferencia atómica ACID exitosa (débito origen y crédito destino simultáneo)", false, e.getMessage());
        }

        // CP-TX-08: Rollback Transaccional ante Fondos Insuficientes en Origen
        Cuenta origAntesRollback = servicio.buscarCuentaPorId(3);
        Cuenta destAntesRollback = servicio.buscarCuentaPorId(4);

        double saldoOrigAntesRB = origAntesRollback.getSaldo();
        double saldoDestAntesRB = destAntesRollback.getSaldo();
        double montoExcesivoTRF = saldoOrigAntesRB + 10000.00;

        boolean lanzoExcepcionRB = false;
        try {
            servicio.realizarTransferencia(3, 4, montoExcesivoTRF, "Transferencia fallida fondos insuficientes");
        } catch (FondosInsuficientesException e) {
            lanzoExcepcionRB = true;
        } catch (BancoException e) {
            lanzoExcepcionRB = true;
        }

        Cuenta origDespuesRB = servicio.buscarCuentaPorId(3);
        Cuenta destDespuesRB = servicio.buscarCuentaPorId(4);

        boolean origSinCambio = Math.abs(origDespuesRB.getSaldo() - saldoOrigAntesRB) < EPSILON;
        boolean destSinCambio = Math.abs(destDespuesRB.getSaldo() - saldoDestAntesRB) < EPSILON;

        afirmar("CP-TX-08: Rollback ACID ante fondos insuficientes en transferencia (saldos intactos)",
                lanzoExcepcionRB && origSinCambio && destSinCambio,
                String.format("Excepcion=%b, OrigenIntacto=%b, DestinoIntacto=%b",
                        lanzoExcepcionRB, origSinCambio, destSinCambio));

        // CP-TX-09: Rollback Transaccional ante Cuenta Destino Bloqueada
        Cuenta origAntesBloq = servicio.buscarCuentaPorId(3);
        Cuenta bloqAntes = servicio.buscarCuentaPorId(5); // Cuenta 5 bloqueada

        double origSaldoPreBloq = origAntesBloq.getSaldo();
        double bloqSaldoPreBloq = bloqAntes.getSaldo();

        boolean lanzoBloqueadaDestino = false;
        try {
            servicio.realizarTransferencia(3, 5, 25.0, "Transferencia hacia cuenta bloqueada");
        } catch (CuentaBloqueadaException e) {
            lanzoBloqueadaDestino = true;
        } catch (BancoException e) {
            lanzoBloqueadaDestino = true;
        }

        Cuenta origPostBloq = servicio.buscarCuentaPorId(3);
        Cuenta bloqPost = servicio.buscarCuentaPorId(5);

        boolean origIntactoBloq = Math.abs(origPostBloq.getSaldo() - origSaldoPreBloq) < EPSILON;
        boolean bloqIntacto = Math.abs(bloqPost.getSaldo() - bloqSaldoPreBloq) < EPSILON;

        afirmar("CP-TX-09: Rollback ACID ante destino BLOQUEADO (origen no debitado y destino intacto)",
                lanzoBloqueadaDestino && origIntactoBloq && bloqIntacto,
                String.format("Excepcion=%b, OrigenNoDebitado=%b, DestinoIntacto=%b",
                        lanzoBloqueadaDestino, origIntactoBloq, bloqIntacto));

        // CP-TX-10: Rollback Transaccional ante Cuenta Destino Inexistente
        double saldoOrigPreInex = servicio.buscarCuentaPorId(3).getSaldo();
        boolean lanzoInexistente = false;
        try {
            servicio.realizarTransferencia(3, 999999, 10.0, "Transferencia a cuenta inexistente");
        } catch (CuentaNoDisponibleException e) {
            lanzoInexistente = true;
        } catch (BancoException e) {
            lanzoInexistente = true;
        }

        double saldoOrigPostInex = servicio.buscarCuentaPorId(3).getSaldo();
        boolean origIntactoInex = Math.abs(saldoOrigPostInex - saldoOrigPreInex) < EPSILON;

        afirmar("CP-TX-10: Rollback ACID ante cuenta destino inexistente (origen intacto)",
                lanzoInexistente && origIntactoInex,
                String.format("Excepcion=%b, OrigenIntacto=%b", lanzoInexistente, origIntactoInex));

        // CP-TX-11: Rechazo de Transferencia a la Misma Cuenta
        boolean lanzoMismaCuenta = false;
        try {
            servicio.realizarTransferencia(3, 3, 20.0, "Transferencia a misma cuenta");
        } catch (TransaccionInvalidaException e) {
            lanzoMismaCuenta = true;
        } catch (BancoException e) {
            lanzoMismaCuenta = true;
        }

        afirmar("CP-TX-11: Rechazo de transferencia con origen y destino idénticos", lanzoMismaCuenta);
    }

    // =========================================================================
    // MÓDULO 5: REPORTES Y TOTALES CONTABLES
    // =========================================================================

    private static void testReportesYTotales(BancoServicio servicio) throws Exception {
        System.out.println("\n--- [MODULO 05: REPORTES Y CONSOLIDADOS CONTABLES] ---");

        // CP-REP-01: Reporte de Cuentas Activas
        List<Cuenta> activas = servicio.reporteCuentasActivas();
        boolean todasActivas = !activas.isEmpty() && activas.stream().allMatch(c -> c.getEstado() == EstadoCuenta.ACTIVA);
        afirmar("CP-REP-01: Reporte de cuentas activas filtra exclusivamente cuentas ACTIVA",
                todasActivas, String.format("Total activas recuperadas: %d", activas.size()));

        // CP-REP-02: Reporte de Cuentas Bloqueadas
        List<Cuenta> bloqueadas = servicio.reporteCuentasBloqueadas();
        boolean todasBloqueadas = !bloqueadas.isEmpty() && bloqueadas.stream().allMatch(c -> c.getEstado() == EstadoCuenta.BLOQUEADA);
        afirmar("CP-REP-02: Reporte de cuentas bloqueadas contiene cuentas en estado BLOQUEADA",
                todasBloqueadas, String.format("Total bloqueadas recuperadas: %d", bloqueadas.size()));

        // CP-REP-03: Reporte de Clientes Registrados Consolidado
        List<Map<String, Object>> clientesResumen = servicio.reporteClientesRegistrados();
        boolean resumenValido = !clientesResumen.isEmpty() && clientesResumen.stream().allMatch(m ->
                m.containsKey("cliente") && m.get("cliente") instanceof Cliente
                && m.containsKey("cantidadCuentas")
                && m.containsKey("saldoTotal")
        );
        afirmar("CP-REP-03: Reporte de clientes registrados incluye cliente, cuentas y saldo consolidado",
                resumenValido, String.format("Total clientes en reporte: %d", clientesResumen.size()));

        // CP-REP-04: Reporte Historial de Transacciones
        List<Transaccion> txs = servicio.reporteHistorialTransacciones();
        boolean historialValido = !txs.isEmpty() && txs.stream().allMatch(t ->
                t.getCodigoTransaccion() != null && !t.getCodigoTransaccion().isEmpty()
                && t.getMonto() > 0.0
                && t.getTipo() != null
        );
        afirmar("CP-REP-04: Historial general de transacciones recupera movimientos con datos consistentes",
                historialValido, String.format("Total transacciones registradas: %d", txs.size()));

        // CP-REP-05: Verificación de Totales Contables Acumulados
        double totalDepositado = servicio.obtenerTotalDepositado();
        double totalRetirado = servicio.obtenerTotalRetirado();
        double totalTransferido = servicio.obtenerTotalTransferido();

        double sumaEsperadaDepositos = txs.stream()
                .filter(t -> t.getTipo() == Transaccion.Tipo.DEPOSITO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double sumaEsperadaRetiros = txs.stream()
                .filter(t -> t.getTipo() == Transaccion.Tipo.RETIRO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double sumaEsperadaTransferencias = txs.stream()
                .filter(t -> t.getTipo() == Transaccion.Tipo.TRANSFERENCIA)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        boolean depositosCoinciden = Math.abs(totalDepositado - sumaEsperadaDepositos) < EPSILON;
        boolean retirosCoinciden = Math.abs(totalRetirado - sumaEsperadaRetiros) < EPSILON;
        boolean transferenciasCoinciden = Math.abs(totalTransferido - sumaEsperadaTransferencias) < EPSILON;

        afirmar("CP-REP-05: Cuadre contable exacto (Total Depositado, Total Retirado, Total Transferido)",
                depositosCoinciden && retirosCoinciden && transferenciasCoinciden,
                String.format("Dep: $%.2f (calc: $%.2f), Ret: $%.2f (calc: $%.2f), Trf: $%.2f (calc: $%.2f)",
                        totalDepositado, sumaEsperadaDepositos,
                        totalRetirado, sumaEsperadaRetiros,
                        totalTransferido, sumaEsperadaTransferencias));

        // CP-REP-06: Estadísticas del Dashboard Gerencial
        Map<String, Object> stats = servicio.obtenerEstadisticasGlobales();
        boolean statsCompletas = stats.containsKey("totalClientes")
                && stats.containsKey("clientesActivos")
                && stats.containsKey("cuentasActivas")
                && stats.containsKey("cuentasBloqueadas")
                && stats.containsKey("saldoTotalCustodia")
                && stats.containsKey("totalDepositado")
                && stats.containsKey("totalRetirado")
                && stats.containsKey("totalTransferido")
                && stats.containsKey("totalTransacciones");

        afirmar("CP-REP-06: Dashboard global consolida métricas clave para la gerencia bancaria",
                statsCompletas, "Claves estadísticas completas");
    }

    // =========================================================================
    // MÓDULO 6: AUDITORÍA Y BITÁCORA CON LOG4J 2
    // =========================================================================

    private static void testLog4j2() {
        System.out.println("\n--- [MODULO 06: AUDITORÍA Y BITÁCORA CON LOG4J 2] ---");

        File logFile = new File("logs/banco.log");
        if (!logFile.exists()) {
            // Si se ejecuta desde un subdirectorio o ruta relativa alternativa
            logFile = new File("Experienciadeaprendizaje3/logs/banco.log");
        }

        boolean archivoExiste = logFile.exists();
        boolean archivoTieneTamano = archivoExiste && logFile.length() > 0;

        afirmar("CP-LOG-01: Creación física del archivo de bitácora logs/banco.log",
                archivoExiste && archivoTieneTamano,
                String.format("Ruta: %s, Tamaño: %d bytes",
                        logFile.getAbsolutePath(), archivoExiste ? logFile.length() : 0));

        boolean tieneInfo = false;
        boolean tieneWarnOError = false;
        int totalLineas = 0;

        if (archivoExiste) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    totalLineas++;
                    if (linea.contains("INFO")) {
                        tieneInfo = true;
                    }
                    if (linea.contains("WARN") || linea.contains("ERROR")) {
                        tieneWarnOError = true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al leer banco.log: " + e.getMessage());
            }
        }

        afirmar("CP-LOG-02: Registro de eventos de nivel INFO en banco.log",
                tieneInfo, String.format("Líneas leídas: %d, Contiene INFO: %b", totalLineas, tieneInfo));

        afirmar("CP-LOG-03: Registro de advertencias y errores (WARN/ERROR) para trazabilidad forense",
                tieneWarnOError, String.format("Contiene WARN o ERROR: %b", tieneWarnOError));
    }

    // =========================================================================
    // UTILIDADES DE AFIRMACIÓN Y REPORTE VISUAL
    // =========================================================================

    private static void afirmar(String descripcion, boolean condicion) {
        afirmar(descripcion, condicion, null);
    }

    private static void afirmar(String descripcion, boolean condicion, String detalle) {
        totalPruebas++;
        if (condicion) {
            pruebasAprobadas++;
            System.out.println(String.format("  [PASS] %s", descripcion));
            if (detalle != null && !detalle.isEmpty()) {
                System.out.println(String.format("         -> Detalle: %s", detalle));
            }
        } else {
            pruebasFallidas++;
            System.err.println(String.format("  [FAIL] %s", descripcion));
            if (detalle != null && !detalle.isEmpty()) {
                System.err.println(String.format("         -> Causa: %s", detalle));
            }
        }
    }
}
