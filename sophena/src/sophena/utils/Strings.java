package sophena.utils;

public class Strings {
	private Strings() {
	}

	/**
	 * Cut a string to the given length. Appends "..." if the string was
	 * truncated.
	 */
	public static String cut(String string, int length) {

		if (string == null || length <= 0)
			return "";

		String str = string.trim();
		if (str.length() <= length)
			return str;

		switch (length) {
		case 1:
			return ".";
		case 2:
			return "..";
		default:
			return str.substring(0, length - 3).concat("...");
		}
	}

	/**
	 * Returns true if both of the given strings are null or if both of the
	 * given strings are equal.
	 */
	public static boolean nullOrEqual(String string1, String string2) {
		return (string1 == null && string2 == null)
				|| (string1 != null && string2 != null && string1
						.equals(string2));
	}

	/**
	 * Returns true if the given string value is null or empty.
	 */
	public static boolean nullOrEmpty(String val) {
		if (val == null)
			return true;
		return val.trim().isEmpty();
	}

	/**
	 * Returns true if the string is not null or empty, means that it contains
	 * other characters than white-spaces.
	 */
	public static boolean notEmpty(String val) {
		if (val == null)
			return false;
		String str = val.trim();
		return !str.isEmpty();
	}

	/**
	 * A null-save method for comparing two strings ignoring the case.
	 */
	public static int compare(String str1, String str2) {
		boolean empty1 = Strings.nullOrEmpty(str1);
		boolean empty2 = Strings.nullOrEmpty(str2);
		if (empty1 && empty2)
			return 0;
		if (!empty1 && empty2)
			return 1;
		if (empty1 && !empty2)
			return -1;
		return str1.compareToIgnoreCase(str2);
	}

}
