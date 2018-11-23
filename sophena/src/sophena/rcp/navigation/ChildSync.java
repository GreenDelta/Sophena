package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import sophena.model.ModelType;
import sophena.model.RootEntity;

class ChildSync {

	private ChildSync() {
	}

	/**
	 * Synchronizes a list of navigation elements with the content of the
	 * database: creates new elements that do not yet exist, removes elements
	 * that are not anymore in the database, and refreshes the descriptors in
	 * the existing elements.
	 *
	 * @param naviContent
	 *            The elements in the navigation tree (a live list).
	 * @param dbContent
	 *            The fresh descriptors from the database.
	 * @param type
	 *            The model type of the content elements to be synchronized.
	 * @param factory
	 *            A factory function for creating new elements.
	 * @param <T>
	 *            The descriptor type of the elements.
	 */
	public static <T extends RootEntity> void sync(
			List<NavigationElement> naviContent,
			List<T> dbContent,
			ModelType type,
			Function<T, ContentElement<T>> factory) {
		if (naviContent == null || dbContent == null || factory == null)
			return;
		List<NavigationElement> synced = new ArrayList<>();
		for (T c : dbContent) {
			ContentElement<T> e = findExisting(naviContent, c);
			if (e == null) {
				e = factory.apply(c);
				naviContent.add(e);
			} else {
				e.content = c;
				e.update();
			}
			synced.add(e);
		}
		removeUnsynced(naviContent, type, synced);
	}

	/**
	 * Remove elements from the navigation content that have the same model type
	 * but are not available in the database anymore.
	 */
	private static void removeUnsynced(List<NavigationElement> naviContent,
			ModelType type, List<NavigationElement> synced) {
		List<NavigationElement> removals = new ArrayList<>();
		for (NavigationElement e : naviContent) {
			if (!(e instanceof ContentElement))
				continue;
			ModelType eType = ContentElement.modelType((ContentElement<?>) e);
			if (eType != type)
				continue;
			if (!synced.contains(e))
				removals.add(e);
		}
		naviContent.removeAll(removals);
	}

	@SuppressWarnings("unchecked")
	private static <T extends RootEntity> ContentElement<T> findExisting(
			List<NavigationElement> elems, T content) {
		if (elems == null || content == null)
			return null;
		for (NavigationElement e : elems) {
			if (!(e instanceof ContentElement))
				continue;
			ContentElement<T> ce = ContentElement.class.cast(e);
			if (Objects.equals(ce.content, content))
				return ce;
		}
		return null;
	}
}
