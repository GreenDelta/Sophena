package sophena.rcp;

import java.text.DecimalFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Numbers {

	private static DecimalFormat format = (DecimalFormat) DecimalFormat
			.getInstance(Locale.GERMAN);

	public static DecimalFormat getFormat() {
		return format;
	}

	public static double read(String text) {
		Number n = readNumber(text);
		return n == null ? 0 : n.doubleValue();
	}

	public static int readInt(String text) {
		Number n = readNumber(text);
		return n == null ? 0 : n.intValue();
	}

	public static Number readNumber(String text) {
		if (text == null || text.trim().isEmpty())
			return null;
		try {
			Number n = format.parse(text.trim());
			return n;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Numbers.class);
			log.trace("invalid number " + text);
			return null;
		}
	}

	public static boolean isNumeric(String text) {
		return readNumber(text) != null;
	}

	public static String toString(double val) {
		return format.format(val);
	}

	public static String toString(int val) {
		return Integer.toString(val);
	}

}
