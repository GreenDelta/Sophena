package sophena.rcp.editors.basedata.transfer.stations;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.TransferStation;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class TransferStationWizard implements IContent {

	private TransferStation station;
	private ProductWizard wizard;

	private Text buildingTypeText;
	private Text capacityText;
	private Text typeText;
	private Text materialText;
	private Text waterHeatingText;
	private Text controlText;

	private TransferStationWizard(TransferStation station) {
		this.station = station;
	}

	static int open(TransferStation station) {
		if (station == null)
			return Window.CANCEL;
		TransferStationWizard content = new TransferStationWizard(station);
		ProductWizard w = new ProductWizard(station, content);
		content.wizard = w;
		w.setWindowTitle("Hausübergabestation");
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(180, 600);
		return d.open();
	}

	@Override
	public void render(Composite c) {

		buildingTypeText = UI.formText(c, M.BuildingType);
		Texts.on(buildingTypeText).required().validate(wizard::validate);
		UI.filler(c);

		capacityText = UI.formText(c, "Leistung");
		Texts.on(capacityText).required().validate(wizard::validate);
		UI.formLabel(c, "kW");

		typeText = UI.formMultiText(c, "Art");
		Texts.on(typeText).required().validate(wizard::validate);
		UI.filler(c);

		materialText = UI.formMultiText(c, "Material");
		Texts.on(materialText).required().validate(wizard::validate);
		UI.filler(c);

		waterHeatingText = UI.formMultiText(c, "Warmwasserbereitung");
		UI.filler(c);

		controlText = UI.formMultiText(c, "Regelung");
		UI.filler(c);

	}

	@Override
	public void bindToUI() {
		Texts.set(buildingTypeText, station.buildingType);
		Texts.set(capacityText, station.outputCapacity);
		Texts.set(typeText, station.stationType);
		Texts.set(materialText, station.material);
		Texts.set(waterHeatingText, station.waterHeating);
		Texts.set(controlText, station.control);
	}

	@Override
	public void bindToModel() {
		station.buildingType = buildingTypeText.getText();
		station.outputCapacity = Texts.getDouble(capacityText);
		station.stationType = typeText.getText();
		station.material = materialText.getText();
		station.waterHeating = waterHeatingText.getText();
		station.control = controlText.getText();
	}

	@Override
	public String validate() {
		if (Texts.isEmpty(buildingTypeText))
			return "Es muss ein Gebäudetyp angegeben werden.";
		if (Texts.isEmpty(capacityText))
			return "Es muss eine Leistung angegeben werden.";
		if (Texts.isEmpty(typeText))
			return "Es muss eine Art angegeben werden.";
		if (Texts.isEmpty(materialText))
			return "Es muss ein Material angegeben werden.";
		return null;
	}

	@Override
	public String getPageName() {
		return "Hausübergabestation";
	}

}
