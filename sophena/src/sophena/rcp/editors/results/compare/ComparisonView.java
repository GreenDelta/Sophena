package sophena.rcp.editors.results.compare;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.Comparison;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ComparisonView extends Editor {

	Comparison comparison;

	public static void open(Comparison comparison) {
		if (comparison == null)
			return;
		String key = App.stash(comparison);
		KeyEditorInput input = new KeyEditorInput(key, "Ergebnisvergleich");
		Editors.open(input, "sophena.ComparisonView");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			KeyEditorInput kei = (KeyEditorInput) input;
			comparison = App.pop(kei.getKey());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init comparison result editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ChartPage(this));
		} catch (Exception e) {

		}
	}

}
