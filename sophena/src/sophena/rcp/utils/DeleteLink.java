package sophena.rcp.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.rcp.Icon;

public class DeleteLink {

	private DeleteLink() {
	}

	public static ImageHyperlink on(Composite comp, Runnable fn) {
		ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
		link.setImage(Icon.DELETE_DISABLED_16.img());
		link.setHoverImage(Icon.DELETE_16.img());
		link.setToolTipText("Entfernen");
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				fn.run();
			}
		});
		return link;
	}
}
