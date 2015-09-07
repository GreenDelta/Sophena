package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class HeatRecoveryEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("heat.recovery.products",
				"Wärmerückgewinnung");
		Editors.open(input, "sophena.products.HeatRecoveryEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.HEAT_RECOVERY));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}
