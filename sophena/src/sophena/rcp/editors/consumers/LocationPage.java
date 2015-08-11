package sophena.rcp.editors.consumers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Location;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

//48.88401, 12.58333

public class LocationPage extends FormPage {

	private Supplier<Location> loc;
	private Editor editor;
	private FormToolkit toolkit;

	public LocationPage(Editor editor, Supplier<Location> location) {
		super(editor, "sophena.LocationPage", M.Location);
		this.editor = editor;
		this.loc = location;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Location);
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createAddressSection(body);
		form.reflow(true);
	}

	private void createAddressSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Standort");
		Location init = loc.get();
		t(c, M.Name, init.name, (s) -> loc.get().name = s);
		t(c, M.Street, init.street, (s) -> loc.get().street = s);
		t(c, M.ZipCode, init.zipCode, (s) -> loc.get().zipCode = s);
		t(c, M.City, init.city, (s) -> loc.get().city = s);
		d(c, "Breitengrad", init.latitude, (d) -> loc.get().latitude = d);
		d(c, "Längengrad", init.longitude, (d) -> loc.get().longitude = d);
	}

	private void t(Composite comp, String label, String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t)
				.init(initial)
				.onChanged((s) -> {
					fn.accept(s);
					editor.setDirty();
				});
	}

	private void d(Composite comp, String label, Double initial,
			Consumer<Double> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t)
				.init(initial)
				.onChanged((s) -> {
					if (Texts.isEmpty(t)) {
						fn.accept(null);
						// delete marker
					} else {
						fn.accept(Texts.getDouble(t));
						// update marker
					}
					editor.setDirty();
				});
	}
}
