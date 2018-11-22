package sophena.rcp.editors.basedata.products;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.Labels;
import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.boilers.BoilerEditor;
import sophena.rcp.editors.basedata.buffers.BufferTankEditor;
import sophena.rcp.editors.basedata.pipes.PipeEditor;
import sophena.rcp.editors.basedata.transfer.stations.TransferStationEditor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

/**
 * The editor page for generic products.
 */
public class ProductEditor extends Editor {

	private ProductType type;

	/**
	 * This function accepts all product types and dispatches to the specific
	 * editors if required.
	 */
	public static void open(ProductType type) {
		if (type == null)
			return;
		switch (type) {
		case BIOMASS_BOILER:
		case FOSSIL_FUEL_BOILER:
		case HEAT_PUMP:
		case COGENERATION_PLANT:
			BoilerEditor.open(type);
			break;
		case HEAT_RECOVERY:
			HeatRecoveryEditor.open();
			break;
		case FLUE_GAS_CLEANING:
			FlueGasCleaningEditor.open();
			break;
		case BUFFER_TANK:
			BufferTankEditor.open();
			break;
		case PIPE:
			PipeEditor.open();
			break;
		case TRANSFER_STATION:
			TransferStationEditor.open();
			break;
		default:
			KeyEditorInput input = new KeyEditorInput(
					type.name(), Labels.get(type));
			Editors.open(input, "sophena.products.ProductEditor");
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof KeyEditorInput) {
			KeyEditorInput kei = (KeyEditorInput) input;
			try {
				type = ProductType.valueOf(kei.getKey());
				setPartName(Labels.getPlural(type));
			} catch (Exception e) {
				throw new PartInitException(
						"Could not parse product type: " + kei.getKey());
			}
		}
		if (type == null)
			throw new PartInitException("No product type: " + input);
		super.init(site, input);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ProductPage(this, type));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}
}
