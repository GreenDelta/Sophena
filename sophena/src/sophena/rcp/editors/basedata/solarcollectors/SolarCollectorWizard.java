package sophena.rcp.editors.basedata.solarcollectors;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.Labels;
import sophena.model.ProductType;
import sophena.model.SolarCollector;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class SolarCollectorWizard implements IContent{
	private final SolarCollector solarCollector;

	private ProductWizard wizard;
	private Text collectorAreaText;
	private Text efficiencyRateRadiationText;
	private Text correctionFactorText;
	private Text heatTransferCoefficient1Text;
	private Text heatTransferCoefficient2Text;
	private Text heatCapacityText;
	private Text angleIncidenceEW10Text; 
	private Text angleIncidenceEW20Text; 
	private Text angleIncidenceEW30Text; 
	private Text angleIncidenceEW40Text; 
	private Text angleIncidenceEW50Text; 
	private Text angleIncidenceEW60Text; 
	private Text angleIncidenceEW70Text; 
	private Text angleIncidenceEW80Text; 
	private Text angleIncidenceEW90Text; 
	private Text angleIncidenceNS10Text; 
	private Text angleIncidenceNS20Text; 
	private Text angleIncidenceNS30Text; 
	private Text angleIncidenceNS40Text; 
	private Text angleIncidenceNS50Text; 
	private Text angleIncidenceNS60Text; 
	private Text angleIncidenceNS70Text; 
	private Text angleIncidenceNS80Text; 
	private Text angleIncidenceNS90Text; 

	private SolarCollectorWizard(SolarCollector solarCollector) {
		this.solarCollector = solarCollector;
	}

	public static int open(SolarCollector solarCollector) {
		if (solarCollector == null || solarCollector.type == null)
			return Window.CANCEL;
		SolarCollectorWizard content = new SolarCollectorWizard(solarCollector);
		ProductWizard w = new ProductWizard(solarCollector, content);
		content.wizard = w;
		w.setWindowTitle(Labels.get(solarCollector.type));
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		return dialog.open();
	}

	@Override
	public void render(Composite c) {
		collectorAreaText = UI.formText(c, M.CollectorArea);
		Texts.on(collectorAreaText).required().decimal().validate(wizard::validate);
		UI.formLabel(c, "m2");
		
		efficiencyRateRadiationText = UI.formText(c, "Wirkungsgrad bzgl. Direktstrahlung");
		Texts.on(efficiencyRateRadiationText).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		correctionFactorText = UI.formText(c, "Korrekturfaktor für Diffusstrahlung");
		Texts.on(correctionFactorText).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		heatTransferCoefficient1Text = UI.formText(c, "Wärmedurchgangskoeffizient 1");
		Texts.on(heatTransferCoefficient1Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		heatTransferCoefficient2Text = UI.formText(c, "Wärmedurchgangskoeffizient 2");
		Texts.on(heatTransferCoefficient2Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		heatCapacityText = UI.formText(c, "Wärmekapazität des Kollektors");
		Texts.on(heatCapacityText).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW10Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 10°");
		Texts.on(angleIncidenceEW10Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW20Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 20°");
		Texts.on(angleIncidenceEW20Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW30Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 30°");
		Texts.on(angleIncidenceEW30Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW40Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 40°");
		Texts.on(angleIncidenceEW40Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW50Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 50°");
		Texts.on(angleIncidenceEW50Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW60Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 60°");
		Texts.on(angleIncidenceEW60Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW70Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 70°");
		Texts.on(angleIncidenceEW70Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW80Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 80°");
		Texts.on(angleIncidenceEW80Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceEW90Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Ost-West 90°");
		Texts.on(angleIncidenceEW90Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS10Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 10°");
		Texts.on(angleIncidenceNS10Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS20Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 20°");
		Texts.on(angleIncidenceNS20Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS30Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 30°");
		Texts.on(angleIncidenceNS30Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS40Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 40°");
		Texts.on(angleIncidenceNS40Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS50Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 50°");
		Texts.on(angleIncidenceNS50Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS60Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 60°");
		Texts.on(angleIncidenceNS60Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS70Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 70°");
		Texts.on(angleIncidenceNS70Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS80Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 80°");
		Texts.on(angleIncidenceNS80Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
		
		angleIncidenceNS90Text = UI.formText(c, "Einfallswinkel-Korrekturfaktor Nord-Süd 90°");
		Texts.on(angleIncidenceNS90Text).required().decimal().validate(wizard::validate);
		UI.filler(c);
	}


	@Override
	public void bindToUI() {
		Texts.set(collectorAreaText, solarCollector.collectorArea);
		Texts.set(efficiencyRateRadiationText, solarCollector.efficiencyRateRadiation);
		Texts.set(correctionFactorText, solarCollector.correctionFactor);
		Texts.set(heatTransferCoefficient1Text, solarCollector.heatTransferCoefficient1);
		Texts.set(heatTransferCoefficient2Text, solarCollector.heatTransferCoefficient2);
		Texts.set(heatCapacityText, solarCollector.heatCapacity);
		Texts.set(angleIncidenceEW10Text, solarCollector.angleIncidenceEW10);
		Texts.set(angleIncidenceEW20Text, solarCollector.angleIncidenceEW20);
		Texts.set(angleIncidenceEW30Text, solarCollector.angleIncidenceEW30);
		Texts.set(angleIncidenceEW40Text, solarCollector.angleIncidenceEW40);
		Texts.set(angleIncidenceEW50Text, solarCollector.angleIncidenceEW50);
		Texts.set(angleIncidenceEW60Text, solarCollector.angleIncidenceEW60);
		Texts.set(angleIncidenceEW70Text, solarCollector.angleIncidenceEW70);
		Texts.set(angleIncidenceEW80Text, solarCollector.angleIncidenceEW80);
		Texts.set(angleIncidenceEW90Text, solarCollector.angleIncidenceEW90);
		Texts.set(angleIncidenceNS10Text, solarCollector.angleIncidenceNS10);
		Texts.set(angleIncidenceNS20Text, solarCollector.angleIncidenceNS20);
		Texts.set(angleIncidenceNS30Text, solarCollector.angleIncidenceNS30);
		Texts.set(angleIncidenceNS40Text, solarCollector.angleIncidenceNS40);
		Texts.set(angleIncidenceNS50Text, solarCollector.angleIncidenceNS50);
		Texts.set(angleIncidenceNS60Text, solarCollector.angleIncidenceNS60);
		Texts.set(angleIncidenceNS70Text, solarCollector.angleIncidenceNS70);
		Texts.set(angleIncidenceNS80Text, solarCollector.angleIncidenceNS80);
		Texts.set(angleIncidenceNS90Text, solarCollector.angleIncidenceNS90);
	}

	@Override
	public void bindToModel() {
		solarCollector.collectorArea = Texts.getDouble(collectorAreaText);
		solarCollector.efficiencyRateRadiation = Texts.getDouble(efficiencyRateRadiationText);
		solarCollector.correctionFactor = Texts.getDouble(correctionFactorText);
		solarCollector.heatTransferCoefficient1 = Texts.getDouble(heatTransferCoefficient1Text);
		solarCollector.heatTransferCoefficient2 = Texts.getDouble(heatTransferCoefficient2Text);
		solarCollector.heatCapacity = Texts.getDouble(heatCapacityText);
		solarCollector.angleIncidenceEW10 = Texts.getDouble(angleIncidenceEW10Text);
		solarCollector.angleIncidenceEW20 = Texts.getDouble(angleIncidenceEW20Text);
		solarCollector.angleIncidenceEW30 = Texts.getDouble(angleIncidenceEW30Text);
		solarCollector.angleIncidenceEW40 = Texts.getDouble(angleIncidenceEW40Text);
		solarCollector.angleIncidenceEW50 = Texts.getDouble(angleIncidenceEW50Text);
		solarCollector.angleIncidenceEW60 = Texts.getDouble(angleIncidenceEW60Text);
		solarCollector.angleIncidenceEW70 = Texts.getDouble(angleIncidenceEW70Text);
		solarCollector.angleIncidenceEW80 = Texts.getDouble(angleIncidenceEW80Text);
		solarCollector.angleIncidenceEW90 = Texts.getDouble(angleIncidenceEW90Text);
		solarCollector.angleIncidenceNS10 = Texts.getDouble(angleIncidenceNS10Text);
		solarCollector.angleIncidenceNS20 = Texts.getDouble(angleIncidenceNS20Text);
		solarCollector.angleIncidenceNS30 = Texts.getDouble(angleIncidenceNS30Text);
		solarCollector.angleIncidenceNS40 = Texts.getDouble(angleIncidenceNS40Text);
		solarCollector.angleIncidenceNS50 = Texts.getDouble(angleIncidenceNS50Text);
		solarCollector.angleIncidenceNS60 = Texts.getDouble(angleIncidenceNS60Text);
		solarCollector.angleIncidenceNS70 = Texts.getDouble(angleIncidenceNS70Text);
		solarCollector.angleIncidenceNS80 = Texts.getDouble(angleIncidenceNS80Text);
		solarCollector.angleIncidenceNS90 = Texts.getDouble(angleIncidenceNS90Text);
	}

	@Override
	public String validate() {
			return validSolarCollector();
	}

	private String validSolarCollector() {
		if (!Texts.hasNumber(collectorAreaText))
			return "Es wurde keine Bruttokollektorfläche angegeben.";

		return null;
	}

	@Override
	public String getPageName() {
		return Labels.get(solarCollector.type);
	}

	@Override
	public ProductType getProductType() {
		return solarCollector.type;
	}
}
