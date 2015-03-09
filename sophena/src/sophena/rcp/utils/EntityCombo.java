package sophena.rcp.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.model.RootEntity;

public class EntityCombo<T extends RootEntity> {

	private String label;
	private ComboViewer viewer;

	public void create(String label, Composite parent) {
		create(label, parent, null);
	}

	public void create(String label, Composite parent, FormToolkit toolkit) {
		this.label = label;
		Combo combo = null;
		if (toolkit == null)
			combo = UI.formCombo(parent, label);
		else
			combo = UI.formCombo(parent, toolkit, label);
		makeViewer(combo);
	}

	public void create(Combo combo) {
		makeViewer(combo);
	}

	private void makeViewer(Combo combo) {
		viewer = new ComboViewer(combo);
		viewer.setLabelProvider(new Label());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}

	public void setInput(List<T> entities) {
		Collections.sort(entities,
				(e1, e2) -> Strings.compare(e1.getName(), e2.getName()));
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

	private class Label extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (!(element instanceof RootEntity))
				return super.getText(element);
			RootEntity entity = (RootEntity) element;
			return entity.getName();
		}
	}

}
