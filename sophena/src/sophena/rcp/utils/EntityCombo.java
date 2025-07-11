package sophena.rcp.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.RootEntity;

public class EntityCombo<T extends RootEntity> {

	private ComboViewer viewer;
	private Function<T, String> labelFn;

	public  EntityCombo<T> create(String label, Composite parent) {
		return create(label, parent, null);
	}

	public  EntityCombo<T> create(String label, Composite comp, FormToolkit tk) {
		var combo = tk == null
				? UI.formCombo(comp, label)
				: UI.formCombo(comp, tk, label);
		viewer = new ComboViewer(combo);
		viewer.setLabelProvider(new Label());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		return this;
	}

	public void setLabelProvider(Function<T, String> labelFn) {
		this.labelFn = labelFn;
	}

	public EntityCombo<T> setInput(List<T> entities) {
		viewer.setInput(entities);
		return this;
	}

	public void onSelect(Consumer<T> listener) {
		viewer.addSelectionChangedListener((e) -> {
			T entity = Viewers.getFirstSelected(viewer);
			if (entity != null)
				listener.accept(entity);
		});
	}

	public EntityCombo<T> select(T entity) {
		if (entity == null)
			return this;
		viewer.setSelection(new StructuredSelection(entity));
		return this;
	}

	public T getSelected() {
		return Viewers.getFirstSelected(viewer);
	}

	public Combo getControl() {
		return viewer != null ? viewer.getCombo() : null;
	}

	private class Label extends LabelProvider {
		@Override
		@SuppressWarnings("unchecked")
		public String getText(Object element) {
			if (!(element instanceof RootEntity))
				return super.getText(element);
			T entity = (T) element;
			if (labelFn == null)
				return entity.name;
			else
				return labelFn.apply(entity);
		}
	}

}
