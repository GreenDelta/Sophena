package sophena.rcp;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.RootEntityDao;
import sophena.model.RootEntity;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Strings;

public class SearchDialog<T extends RootEntity> extends FormDialog {

	private T selection;
	private String title;
	private List<T> list;
	private Function<T, String> labelFn;

	private Text filterText;
	private ListViewer viewer;

	public static <T extends RootEntity> T open(String title, Class<T> clazz,
			Function<T, String> labelFn) {
		RootEntityDao<T> dao = new RootEntityDao<>(clazz, App.getDb());
		List<T> all = dao.getAll();
		return open(title, all, labelFn);
	}

	public static <T extends RootEntity> T open(String title, List<T> list,
			Function<T, String> labelFn) {
		if (list == null)
			return null;
		SearchDialog<T> d = new SearchDialog<>();
		d.list = list;
		d.title = title;
		d.labelFn = labelFn;
		if (d.open() == OK)
			return d.selection;
		else
			return null;
	}

	private SearchDialog() {
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
		UI.formHeader(mform, title);
		Composite body = UI.formBody(mform.getForm(), tk);
		Composite c = UI.formComposite(body, tk);
		UI.gridData(c, true, false);
		filterText = UI.formText(c, tk, "Suche");
		Texts.on(filterText).onChanged((s) -> viewer.refresh());
		createViewer(body, tk);
		viewer.setInput(list);
		viewer.addSelectionChangedListener((e) -> {
			selection = Viewers.getFirst(e.getSelection());
			Button ok = getButton(IDialogConstants.OK_ID);
			ok.setEnabled(selection != null);
		});
	}

	private void createViewer(Composite body, FormToolkit tk) {
		Composite c = tk.createComposite(body);
		UI.gridData(c, true, true);
		UI.gridLayout(c, 1);
		viewer = new ListViewer(c);
		UI.gridData(viewer.getList(), true, true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setFilters(new ViewerFilter[] { new Filter() });
		viewer.setSorter(new Sorter());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object obj) {
				if (obj == null)
					return null;
				T e = (T) obj;
				return labelFn.apply(e);
			}
		});
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

	private class Filter extends ViewerFilter {

		@Override
		@SuppressWarnings("unchecked")
		public boolean select(Viewer viewer, Object parent, Object obj) {
			if (obj == null)
				return false;
			if (Texts.isEmpty(filterText))
				return true;
			String label = labelFn.apply((T) obj);
			if (Strings.nullOrEmpty(label))
				return false;
			String[] parts = filterText.getText().trim().toLowerCase()
					.split(" ");
			String n = label.toLowerCase();
			for (String part : parts) {
				if (!n.contains(part))
					return false;
			}
			return true;
		}
	}

	private class Sorter extends ViewerSorter {

		@Override
		@SuppressWarnings("unchecked")
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			String label1 = labelFn.apply((T) o1);
			String label2 = labelFn.apply((T) o1);
			return Strings.compare(label1, label2);
		}
	}

}
