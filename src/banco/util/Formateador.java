package banco.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Formateador {

    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FORMATO_FECHA_CORTA = new SimpleDateFormat("dd/MM/yyyy");

    private Formateador() {}

    public static String aMoneda(double monto) {
        return FORMATO_MONEDA.format(monto);
    }

    public static String aFecha(Date fecha) {
        if (fecha == null) return "N/D";
        return FORMATO_FECHA.format(fecha);
    }

    public static String aFechaCorta(Date fecha) {
        if (fecha == null) return "N/D";
        return FORMATO_FECHA_CORTA.format(fecha);
    }
}
