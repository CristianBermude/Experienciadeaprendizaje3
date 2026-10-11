package banco.util;

import banco.excepciones.DatoInvalidoException;
import java.util.regex.Pattern;

/**
 * Clase utilitaria con validaciones estrictas para el dominio salvadoreño y bancario.
 */
public final class Validador {

    private static final Pattern PATRON_DUI = Pattern.compile("^\\d{8}-\\d$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[267]\\d{3}-\\d{4}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PATRON_NUMERO_CUENTA = Pattern.compile("^[A-Z0-9-]{6,25}$");

    private Validador() {
        // Constructor privado para clase utilitaria
    }

    public static void validarTextoNoVacio(String texto, String nombreCampo) throws DatoInvalidoException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo '" + nombreCampo + "' es obligatorio y no puede estar vacío.");
        }
    }

    public static void validarCodigoCliente(String codigo) throws DatoInvalidoException {
        validarTextoNoVacio(codigo, "Código de Cliente");
        if (codigo.trim().length() < 3 || codigo.trim().length() > 20) {
            throw new DatoInvalidoException("El código de cliente debe tener entre 3 y 20 caracteres.");
        }
    }

    public static void validarDui(String dui) throws DatoInvalidoException {
        validarTextoNoVacio(dui, "DUI");
        String duiLimpio = dui.trim();
        if (!PATRON_DUI.matcher(duiLimpio).matches()) {
            throw new DatoInvalidoException("El DUI '" + dui + "' no es válido. Debe tener el formato salvadoreño 00000000-0 (8 dígitos, guion y dígito verificador).");
        }
    }

    public static void validarTelefono(String telefono) throws DatoInvalidoException {
        validarTextoNoVacio(telefono, "Teléfono");
        String telLimpio = telefono.trim();
        if (!PATRON_TELEFONO.matcher(telLimpio).matches()) {
            throw new DatoInvalidoException("El teléfono '" + telefono + "' debe tener el formato 0000-0000 y comenzar con 2, 6 o 7.");
        }
    }

    public static void validarCorreo(String correo) throws DatoInvalidoException {
        validarTextoNoVacio(correo, "Correo electrónico");
        String correoLimpio = correo.trim();
        if (!PATRON_CORREO.matcher(correoLimpio).matches()) {
            throw new DatoInvalidoException("El correo electrónico '" + correo + "' no tiene un formato válido (ejemplo: usuario@dominio.com).");
        }
    }

    public static void validarNumeroCuenta(String numeroCuenta) throws DatoInvalidoException {
        validarTextoNoVacio(numeroCuenta, "Número de Cuenta");
        String numLimpio = numeroCuenta.trim().toUpperCase();
        if (!PATRON_NUMERO_CUENTA.matcher(numLimpio).matches()) {
            throw new DatoInvalidoException("El número de cuenta '" + numeroCuenta + "' debe contener entre 6 y 25 caracteres alfanuméricos o guiones.");
        }
    }

    public static void validarSaldoInicial(double saldo) throws DatoInvalidoException {
        if (Double.isNaN(saldo) || saldo < 0.0) {
            throw new DatoInvalidoException("El saldo inicial no puede ser negativo ni inválido. Valor ingresado: $" + String.format("%.2f", saldo));
        }
    }
}
