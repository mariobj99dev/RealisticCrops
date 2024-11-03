package net.maxrio22.realisticcrops;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static long timeToMilliseconds(String amount) {

        final int MILLISECONDS_PER_SECOND = 1000;
        final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
        final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
        final int MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;
        final int MILLISECONDS_PER_WEEK = MILLISECONDS_PER_DAY * 7;
        final int MILLISECONDS_PER_MONTH = MILLISECONDS_PER_DAY * 30; // Aproximado
        final int MILLISECONDS_PER_YEAR = MILLISECONDS_PER_DAY * 365; // Aproximado

        long totalMilliseconds = 0;

        // Expresión regular para extraer el número y la unidad (ej. "1y", "2w", "3d")
        Pattern pattern = Pattern.compile("(\\d+)([yMwdhms])");
        Matcher matcher = pattern.matcher(amount);

        // Recorre cada coincidencia en el texto de entrada
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1)); // Captura el número
            String unit = matcher.group(2); // Captura la unidad

            // Convierte cada unidad a milisegundos
            switch (unit) {
                case "y" -> totalMilliseconds += (long) value * MILLISECONDS_PER_YEAR;
                case "M" -> totalMilliseconds += (long) value * MILLISECONDS_PER_MONTH;
                case "w" -> totalMilliseconds += (long) value * MILLISECONDS_PER_WEEK;
                case "d" -> totalMilliseconds += (long) value * MILLISECONDS_PER_DAY;
                case "h" -> totalMilliseconds += (long) value * MILLISECONDS_PER_HOUR;
                case "m" -> totalMilliseconds += (long) value * MILLISECONDS_PER_MINUTE;
                case "s" -> totalMilliseconds += (long) value * MILLISECONDS_PER_SECOND;
                default -> throw new IllegalArgumentException("Unidad desconocida: " + unit);
            }
        }

        return totalMilliseconds;
    }
}
