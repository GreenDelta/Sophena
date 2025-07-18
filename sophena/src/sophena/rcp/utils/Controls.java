package sophena.rcp.utils;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import sophena.model.ProductGroup;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.basedata.ProductGroupEditor;

public final class Controls {

	private Controls() {
	}

	public static void renderGroupLink(
			ProductGroup group, FormToolkit tk, Composite comp
	) {
		UI.formLabel(comp, tk, "Produktgruppe");
		var link = tk.createImageHyperlink(comp, SWT.NONE);
		if (group != null) {
			link.setText(group.name);
		} else {
			link.setText("FEHLER: keine Produktgruppe");
		}
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> ProductGroupEditor.open());
	}

	public static void onClick(Hyperlink link, Consumer<HyperlinkEvent> fn) {
		if (link == null || fn == null)
			return;
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				fn.accept(e);
			}
		});
	}

	public static void onSelect(Combo combo, Consumer<SelectionEvent> consumer) {
		combo.addSelectionListener(createSelectionListener(consumer));
	}

	public static void onSelect(Button button, Consumer<SelectionEvent> consumer) {
		button.addSelectionListener(createSelectionListener(consumer));
	}

	public static void onSelect(MenuItem item, Consumer<SelectionEvent> consumer) {
		item.addSelectionListener(createSelectionListener(consumer));
	}

	public static void onSelect(Scale scale, Consumer<SelectionEvent> consumer) {
		scale.addSelectionListener(createSelectionListener(consumer));
	}

	public static void onSelect(Link link, Consumer<SelectionEvent> consumer) {
		link.addSelectionListener(createSelectionListener(consumer));
	}

	public static void onSelect(Spinner spinner,
			Consumer<SelectionEvent> consumer) {
		spinner.addSelectionListener(createSelectionListener(consumer));
	}

	private static SelectionListener createSelectionListener(
			Consumer<SelectionEvent> consumer) {
		return new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				consumer.accept(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				consumer.accept(e);
			}
		};
	}

}
