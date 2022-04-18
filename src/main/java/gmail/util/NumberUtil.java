package gmail.util;

import java.text.DecimalFormat;

/**
 * Created by emp350 on 4/02/21
 */
public class NumberUtil {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final DecimalFormat integerFormat = new DecimalFormat("0");
    private static final DecimalFormat decimal = new DecimalFormat("#.#####");

    /**
     * Checks if String contain number
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String formatDecimal(double d) {
        if (d == 0) {
            return "0.00";
        }
        return decimalFormat.format(d);
    }

    public static String formatDecimalWithoutZeros(double d) {
        return decimal.format(d);
    }

    public static String formatDecimalToInteger(double d) {
        if (d == 0) {
            return "0";
        }
        return integerFormat.format(d);
    }

    public static double kleinSum(double[] input) {
        double s = 0.0, cs = 0.0, ccs = 0.0, c, cc;
        for (int i = 0; i < input.length; i++) {
            double t = s + input[i];
            if (Math.abs(s) >= Math.abs(input[i])) {
                c = (s - t) + input[i];
            } else {
                c = (input[i] - t) + s;
            }
            s = t;
            t = cs + c;
            if (Math.abs(cs) >= Math.abs(c)) {
                cc = (cs - t) + c;
            } else {
                cc = (c - t) + cs;
            }
            cs = t;
            ccs = ccs + cc;
        }
        return s + cs + ccs;
    }

    public static String convertBytes(int bytes) {
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        if (tb >= 1) {
            return decimalFormat.format(tb).concat(" TB");
        } else if (gb >= 1) {
            return decimalFormat.format(gb).concat(" GB");
        } else if (mb >= 1) {
            return decimalFormat.format(mb).concat(" MB");
        } else if (kb >= 1) {
            return decimalFormat.format(kb).concat(" KB");
        } else {
            return bytes + " bytes";
        }
    }

}
