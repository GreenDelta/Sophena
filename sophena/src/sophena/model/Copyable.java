package sophena.model;

public interface Copyable<T extends Copyable<T>> {

	T copy();

}
