package sophena.utils;

/**
 * A helper class for setting values to local variables within closures or
 * anonymous classes.
 */
public final class Ref<T> {

	private T value;

	public static <T> Ref<T> of(T value) {
		Ref<T> ref = new Ref<>();
		ref.value = value;
		return ref;
	}

	public Ref() {
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

}
