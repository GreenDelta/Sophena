package sophena.rcp.editors.results.compare;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

import sophena.io.excel.ComparisonExport;
import sophena.rcp.Icon;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Popup;
import sophena.rcp.utils.Rcp;

public class ComparisonToolBar extends EditorActionBarContributor {

	@Override
	public void contributeToToolBar(IToolBarManager toolbar) {
		Action export = Actions.create("Nach Excel exportieren",
				Icon.EXCEL_16.des(), this::ExcelExportCompare);
		toolbar.add(export);
	}

	private void ExcelExportCompare() {
		ComparisonView editor = Editors.getActive();
		if (editor == null)
			return;
		File file = FileChooser.save("Ergebnisvergleich.xlsx", "*.xlsx");
		if (file == null)
			return;
		ComparisonExport export = new ComparisonExport(editor.comparison, file);
		Rcp.run("Exportiere Ergebnisse ...", export, () -> {
			Popup.showInfo("Ergebnisse wurden exportiert");
		});
	}
}
