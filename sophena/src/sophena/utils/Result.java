package sophena.utils;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Similar to the result type in Rust: https://doc.rust-lang.org/std/result/
 * but with plain strings as errors and an additional warning state. `ok` and
 * `warning` mean that a result is present. `error` indicates an error instead
 * of a result. Messages are optional.
 */
public class Result<T> {

	private static final int OK = 0;
	private static final int WARNING = 1;
	private static final int ERROR = 2;

	private final int type;
	private final String message;
	private final T result;

	private Result(int type, T result, String message) {
		this.type = type;
		this.result = result;
		this.message = message;
	}

	public static <T> Result<T> ok(T result) {
		if (result == null)
			throw new IllegalArgumentException(
					"ok cannot be called with null");
		return new Result<>(OK, result, null);
	}

	public static <T> Result<T> ok(T result, String message) {
		if (result == null)
			throw new IllegalArgumentException(
					"ok cannot be called with null");
		return new Result<>(OK, result, message);
	}

	public static <T> Result<T> warning(T result) {
		if (result == null)
			throw new IllegalArgumentException(
					"warning cannot be called with null");
		return new Result<>(WARNING, result, null);
	}

	public static <T> Result<T> warning(T result, String message) {
		if (result == null)
			throw new IllegalArgumentException(
					"warning cannot be called with null");
		return new Result<>(WARNING, result, null);
	}

	public static <T> Result<T> error() {
		return new Result<>(ERROR, null, null);
	}

	public static <T> Result<T> error(String message) {
		return new Result<>(ERROR, null, message);
	}

	public T get() {
		if (result == null)
			throw new NoSuchElementException(
					"No result present");
		return result;
	}

	public Optional<String> message() {
		return Optional.ofNullable(message);
	}

	public boolean isPresent() {
		return result != null;
	}

	public boolean isOK() {
		return type == OK;
	}

	public boolean isWarning() {
		return type == WARNING;
	}

	public boolean isError() {
		return type == ERROR;
	}

	public Optional<T> toOptional() {
		return Optional.ofNullable(result);
	}
}
