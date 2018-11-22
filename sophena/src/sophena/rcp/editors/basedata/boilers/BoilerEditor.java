package sophena.rcp.editors.basedata.boilers;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.Labels;
import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.utils.Logs;

public class BoilerEditor extends Editor {

	private ProductType type;

	public static void open(ProductType type) {
		if (!valid(type))
			return;
		Editors.open(new Input(type), "sophena.BoilerEditor");
	}

	private static boolean valid(ProductType type) {
		if (type == null)
			return false;
		switch (type) {
		case BIOMASS_BOILER:
		case FOSSIL_FUEL_BOILER:
		case HEAT_PUMP:
		case COGENERATION_PLANT:
			return true;
		default:
			Logs.error(BoilerEditor.class, "invalid product type {}", type);
			return false;
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		Input in = (Input) input;
		type = in.type;
		setPartName(Labels.getPlural(type));
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, type));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private static class Input extends KeyEditorInput {

		final ProductType type;

		public Input(ProductType type) {
			super("data.boilers." + type.name(), Labels.getPlural(type));
			this.type = type;
		}
	}

}
