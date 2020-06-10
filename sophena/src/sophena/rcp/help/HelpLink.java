package sophena.rcp.help;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.rcp.Icon;
import sophena.rcp.utils.Controls;

public class HelpLink {

	private HelpLink() {
	}

	public static void create(Composite comp, String title, String helpKey) {
		create(comp, null, title, helpKey);
	}

	public static void create(Composite comp, FormToolkit tk, String title,
			String helpKey) {
		ImageHyperlink link = tk == null
				? new ImageHyperlink(comp, SWT.NONE)
				: tk.createImageHyperlink(comp, SWT.NONE);
		link.setImage(Icon.INFO_16.img());
		Controls.onClick(link, e -> HelpBox.show(title, helpKey));
	}
}
