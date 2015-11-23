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

	public void create(String label, Composite parent) {
		create(label, parent, null);
	}

	public void create(String label, Composite parent, FormToolkit toolkit) {
		Combo combo = null;
		if (toolkit == null)
			combo = UI.formCombo(parent, label);
		else
			combo = UI.formCombo(parent, toolkit, label);
		viewer = new ComboViewer(combo);
		viewer.setLabelProvider(new Label());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}

	public void setLabelProvider(Function<T, String> labelFn) {
		this.labelFn = labelFn;
	}

	public void setInput(List<T> entities) {
		viewer.setInput(entities);
	}

	public void onSelect(Consumer<T> listener) {
		viewer.addSelectionChangedListener((e) -> {
			T entity = Viewers.getFirstSelected(viewer);
			if (entity != null)
				listener.accept(entity);
		});
	}

	public void select(T entity) {
		if (entity == null)
			return;
		viewer.setSelection(new StructuredSelection(entity));
	}

	public T getSelected() {
		return Viewers.getFirstSelected(viewer);
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
