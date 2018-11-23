package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.editors.results.CalculationCheck;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Ref;

/**
 * A dialog to set up a project comparison.
 */
public class ComparisonDialog extends FormDialog {

	private TableViewer table;
	private Map<String, Boolean> selected = new HashMap<>();
	private List<ProjectDescriptor> descriptors;

	public static void open(Optional<ProjectDescriptor> initial) {
		ComparisonDialog dialog = new ComparisonDialog();
		if (initial.isPresent()) {
			ProjectDescriptor d = initial.get();
			dialog.selected.put(d.id, Boolean.TRUE);
		}
		dialog.open();
	}

	private ComparisonDialog() {
		super(UI.shell());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, false).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		UI.formHeader(mform, "Projekte und Alternativen vergleichen");
		Composite body = UI.formBody(mform.getForm(), tk);
		table = Tables.createViewer(body, "Name");
		table.setLabelProvider(new Label());
		Table t = table.getTable();
		t.setLinesVisible(false);
		t.setHeaderVisible(false);
		Tables.bindColumnWidths(t, 1.0);
		UI.gridData(table.getTable(), true, true);
		Tables.onClick(table, this::itemClicked);
		ProjectDao dao = new ProjectDao(App.getDb());
		descriptors = dao.getDescriptors();
		Sorters.byName(descriptors);
		table.setInput(descriptors);
	}

	private void itemClicked(MouseEvent e) {
		ProjectDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		Boolean b = selected.get(d.id);
		if (b == null || !b.booleanValue())
			selected.put(d.id, Boolean.TRUE);
		else
			selected.put(d.id, Boolean.FALSE);
		table.refresh();
		int count = 0;
		for (Boolean val : selected.values()) {
			if (Objects.equals(Boolean.TRUE, val))
				count++;
		}
		getButton(IDialogConstants.OK_ID).setEnabled(count > 1);
	}

	@Override
	protected void okPressed() {
		List<Project> list = new ArrayList<>();
		ProjectDao dao = new ProjectDao(App.getDb());
		for (ProjectDescriptor d : descriptors) {
			Boolean s = selected.get(d.id);
			if (!Boolean.TRUE.equals(s))
				continue;
			Project p = dao.get(d.id);
			list.add(p);
		}
		if (!CalculationCheck.canCalculate(list))
			return;
		Ref<Comparison> ref = new Ref<>();
		Rcp.run("Vergleiche Projekte",
				() -> ref.set(Comparison.calculate(list)),
				() -> ComparisonView.open(ref.get()));
		close();
	}

	@Override
	protected void cancelPressed() {
		close();
	}

	@Override
	protected Point getInitialSize() {
		int width = 600;
		int height = 600;
		Rectangle shellBounds = getShell().getDisplay().getBounds();
		int shellWidth = shellBounds.x;
		int shellHeight = shellBounds.y;
		if (shellWidth > 0 && shellWidth < width)
			width = shellWidth;
		if (shellHeight > 0 && shellHeight < height)
			height = shellHeight;
		return new Point(width, height);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point loc = super.getInitialLocation(initialSize);
		int marginTop = (getParentShell().getSize().y - initialSize.y) / 3;
		if (marginTop < 0)
			marginTop = 0;
		return new Point(loc.x, loc.y + marginTop);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof ProjectDescriptor))
				return null;
			ProjectDescriptor d = (ProjectDescriptor) obj;
			Boolean entry = selected.get(d.id);
			return entry != null && entry.booleanValue()
					? Icon.CHECKBOX_CHECKED_16.img()
					: Icon.CHECKBOX_UNCHECKED_16.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ProjectDescriptor))
				return null;
			ProjectDescriptor d = (ProjectDescriptor) obj;
			return d.name;
		}

	}

}
