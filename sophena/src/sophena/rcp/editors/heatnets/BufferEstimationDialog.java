package sophena.rcp.editors.heatnets;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import sophena.model.BufferTank;
import sophena.rcp.app.Icon;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class BufferEstimationDialog extends FormDialog {

	private final List<BufferCosts> costs;
	private BufferTank selection;

	static Optional<BufferTank> open(List<BufferCosts> costs) {
		if (costs == null || costs.isEmpty()) {
			return Optional.empty();
		}
		var dialog = new BufferEstimationDialog(costs);
		return dialog.open() == OK
			? Optional.ofNullable(dialog.selection)
			: Optional.empty();
	}

	private BufferEstimationDialog(List<BufferCosts> costs) {
		super(UI.shell());
		this.costs = costs;
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
		UI.formHeader(mform, "Mögliche Pufferspeicher");
		var body = UI.formBody(mform.getForm(), tk);

		var table = Tables.createViewer(body,
			"Hersteller / Bezeichnung",
			"Volumen",
			"Wärmegestehungskosten",
			"Ungedeckte Wärme");
		Tables.bindColumnWidths(table, 0.30, 0.20, 0.25, 0.25);
		Tables.rightAlignColumns(table, 1, 2, 3);
		table.setLabelProvider(new Label());
		table.setInput(costs);

		table.addSelectionChangedListener(e -> {
			BufferCosts bc = Viewers.getFirst(e.getSelection());
			selection = bc != null ? bc.buffer() : null;
			getButton(IDialogConstants.OK_ID).setEnabled(selection != null);
		});

		table.addDoubleClickListener(e -> {
			BufferCosts bc = Viewers.getFirst(e.getSelection());
			selection = bc != null ? bc.buffer() : null;
			if (selection != null) {
				okPressed();
			}
		});
	}

	@Override
	protected Point getInitialSize() {
		return new Point(850, 500);
	}

	private static class Label extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Icon.BUFFER_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BufferCosts(
				BufferTank buffer, double costs, double uncoveredHeat
			)) || buffer == null) {
				return null;
			}

			return switch (col) {
				case 0 -> buffer.manufacturer != null
					? buffer.manufacturer.name + " / " + buffer.name
					: buffer.name;
				case 1 -> Num.str(buffer.volume) + " L";
				case 2 -> Num.str(costs) + " EUR/MWh";
				case 3 -> Num.str(uncoveredHeat) + " kWh";
				default -> null;
			};
		}
	}
}
