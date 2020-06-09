package sophena.utils;

public final class Enums {

	private Enums() {
	}

	/**
	 * Compares the given enumeration constants by their ordinal values which
	 * are the respective positions of the constants in the enumeration
	 * declaration.
	 */
	public static <T extends Enum<T>> int compare(T e1, T e2) {
		if (e1 == null && e2 == null)
			return 0;
		if (e1 == null)
			return -1;
		if (e2 == null)
			return 1;
		return e1.ordinal() - e2.ordinal();
	}

}
