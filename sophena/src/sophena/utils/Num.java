package sophena.utils;

import java.text.DecimalFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Num {

	private static DecimalFormat format;
	private static DecimalFormat intFormat;

	public static DecimalFormat getFormat() {
		if (format == null) {
			format = (DecimalFormat) DecimalFormat
					.getInstance(Locale.GERMAN);
			format.applyLocalizedPattern("#.###.###.###.###,##");
		}
		return format;
	}

	private static DecimalFormat getIntFormat() {
		if (intFormat == null) {
			intFormat = (DecimalFormat) DecimalFormat
					.getInstance(Locale.GERMAN);
			intFormat.applyLocalizedPattern("#.###.###.###.###");
		}
		return intFormat;
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
			Number n = getFormat().parse(text.trim());
			return n;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Num.class);
			log.trace("invalid number " + text);
			return null;
		}
	}

	public static boolean isNumeric(String text) {
		return readNumber(text) != null;
	}

	public static String str(Double val) {
		if (val == null)
			return "";
		return str(val.doubleValue());
	}

	public static String str(double val) {
		return getFormat().format(val);
	}

	public static String intStr(Double val) {
		if (val == null)
			return "";
		return intStr(val.doubleValue());
	}

	public static String intStr(double val) {
		return getIntFormat().format(val);
	}

	public static String intStr(Integer val) {
		if (val == null)
			return "";
		return intStr(val.intValue());
	}

	public static String intStr(int val) {
		return getIntFormat().format(val);
	}

}
