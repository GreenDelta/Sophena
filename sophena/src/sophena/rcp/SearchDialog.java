package sophena.rcp;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.RootEntityDao;
import sophena.model.BufferTank;
import sophena.model.FlueGasCleaning;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.model.RootEntity;
import sophena.model.TransferStation;
import sophena.rcp.utils.Sorters;
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

	/**
	 * Creates a search dialog for the given list of entities.
	 * Note that you should sort the list before passing it
	 * into this dialog. See also the other factory methods
	 * in this class.
	 */
	public static <T extends RootEntity> T open(
			String title,
			List<T> list,
			Function<T, String> labelFn) {
		if (list == null)
			return null;
		var dialog = new SearchDialog<T>();
		dialog.list = list;
		dialog.title = title;
		dialog.labelFn = labelFn;
		return dialog.open() == OK
				? dialog.selection
				: null;
	}

	public static BufferTank forBuffers() {
		var buffers = new RootEntityDao<>(
				BufferTank.class, App.getDb()).getAll();
		Sorters.buffers(buffers);
		return open(
				"Pufferspeicher",
				buffers,
				SearchLabel::forBufferTank);
	}

	public static TransferStation forTransferStations() {
		var stations = new RootEntityDao<>(
				TransferStation.class, App.getDb()).getAll();
		Sorters.transferStations(stations);
		return open(
				"Hausübergabestationen",
				stations,
				SearchLabel::forTransferStation);
	}

	public static Pipe forPipes() {
		var pipes = new RootEntityDao<>(
				Pipe.class, App.getDb()).getAll();
		Sorters.pipes(pipes);
		return open(
				"Wärmeleitungen",
				pipes,
				SearchLabel::forPipe);
	}

	public static HeatRecovery forHeatRecoveries() {
		var recoveries = new RootEntityDao<>(
				HeatRecovery.class, App.getDb()).getAll();
		Sorters.heatRecoveries(recoveries);
		return open(
				"Wärmerückgewinnungen",
				recoveries,
				SearchLabel::forHeatRecovery);
	}

	public static FlueGasCleaning forFlueGasCleanings() {
		var cleanings = new RootEntityDao<>(
				FlueGasCleaning.class, App.getDb()).getAll();
		Sorters.flueGasCleanings(cleanings);
		return open(
				"Rauchgasreinigungen",
				cleanings,
				SearchLabel::forFlueGasCleaning);
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
		var tk = mform.getToolkit();
		UI.formHeader(mform, title);
		var body = UI.formBody(mform.getForm(), tk);
		var comp = UI.formComposite(body, tk);
		UI.gridData(comp, true, false);
		filterText = UI.formText(comp, tk, "Suche");
		Texts.on(filterText).onChanged(s -> viewer.refresh());
		createViewer(body, tk);
		viewer.setInput(list);
		viewer.addSelectionChangedListener((e) -> {
			selection = Viewers.getFirst(e.getSelection());
			var ok = getButton(IDialogConstants.OK_ID);
			ok.setEnabled(selection != null);
		});
		viewer.addDoubleClickListener(e -> {
			selection = Viewers.getFirst(e.getSelection());
			if (selection != null) {
				okPressed();
			}
		});
	}

	private void createViewer(Composite body, FormToolkit tk) {
		Composite c = tk.createComposite(body);
		UI.gridData(c, true, true);
		UI.gridLayout(c, 1);
		viewer = new ListViewer(c);
		UI.gridData(viewer.getList(), true, true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setFilters(new Filter());
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
}
