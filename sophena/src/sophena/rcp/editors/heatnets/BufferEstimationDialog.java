package sophena.rcp.editors.heatnets;

import java.util.ArrayList;
import java.util.Comparator;
import sophena.model.BufferTank;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import sophena.rcp.app.Icon;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class BufferEstimationDialog extends FormDialog {

	private List<BufferCosts> costs;
	private BufferTank selection;
	private TableViewer table;

	static Optional<BufferTank> open(List<BufferCosts> costs) {
		if (costs == null || costs.isEmpty()) {
			return Optional.empty();
		}
		var dialog = new BufferEstimationDialog();
		dialog.costs = new ArrayList<>(costs);
		dialog.costs.sort(Comparator
				.comparingDouble(BufferCosts::costs)
				.thenComparingDouble(BufferCosts::uncoveredHeat));
		return dialog.open() == OK
				? Optional.ofNullable(dialog.selection)
				: Optional.empty();
	}

	private BufferEstimationDialog() {
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
		UI.formHeader(mform, "Pufferspeicher auswählen");
		var body = UI.formBody(mform.getForm(), tk);
		table = Tables.createViewer(body,
				"Hersteller", "Volumen", "Wärmegestehungskosten", "Ungedeckte Wärme", "Bezeichnung");
		Tables.bindColumnWidths(table, 0.22, 0.14, 0.2, 0.18, 0.26);
		Tables.rightAlignColumns(table, 1, 2, 3);
		table.setLabelProvider(new Label());
		table.setInput(costs);
		table.addSelectionChangedListener(e -> {
			var selected = Viewers.<BufferCosts>getFirst(e.getSelection());
			selection = selected != null ? selected.buffer() : null;
			getButton(IDialogConstants.OK_ID).setEnabled(selection != null);
		});
		table.addDoubleClickListener(e -> {
			var selected = Viewers.<BufferCosts>getFirst(e.getSelection());
			selection = selected != null ? selected.buffer() : null;
			if (selection != null) {
				okPressed();
			}
		});
	}

	@Override
	protected Point getInitialSize() {
		int width = 850;
		int height = 500;
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

	private static class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Icon.BUFFER_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BufferCosts costs))
				return null;
			var buffer = costs.buffer();
			if (buffer == null)
				return null;
			switch (col) {
			case 0:
				return buffer.manufacturer != null
						? buffer.manufacturer.name
						: null;
			case 1:
				return Num.str(buffer.volume) + " L";
			case 2:
				return Num.str(costs.costs()) + " EUR/MWh";
			case 3:
				return Num.str(costs.uncoveredHeat()) + " kWh";
			case 4:
				return buffer.name;
			default:
				return null;
			}
		}
	}

}
