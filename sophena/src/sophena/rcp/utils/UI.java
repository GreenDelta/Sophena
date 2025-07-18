package sophena.rcp.utils;

import java.util.function.Function;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.LoggerFactory;

public class UI {

	private UI() {
	}

	public static Shell shell() {
		Shell shell = null;
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		if (display != null)
			shell = display.getActiveShell();
		if (shell == null)
			if (display != null)
				shell = new Shell(display);
			else
				shell = new Shell();
		return shell;
	}

	/**
	 * Calls the given function when the browser has loaded the given URL. No
	 * threads are spawned here. You have to make sure that the given function
	 * accesses the browser in the UI thread.
	 */
	public static void onLoaded(Browser browser, String url, Runnable fn) {
		if (browser == null || url == null)
			return;
		browser.addProgressListener(new ProgressListener() {

			@Override
			public void completed(ProgressEvent event) {
				if (fn != null) {
					fn.run();
					browser.removeProgressListener(this);
				}
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
		browser.setUrl(url);
	}

	/**
	 * Bind the given function with the given name to the `window` object of the
	 * given browser.
	 */
	public static void bindFunction(Browser browser, String name,
																	Function<Object[], Object> fn) {
		if (browser == null || name == null || fn == null)
			return;
		var func = new BrowserFunction(browser, name) {
			@Override
			public Object function(Object[] args) {
				try {
					return fn.apply(args);
				} catch (Exception e) {
					var log = LoggerFactory.getLogger(UI.class);
					log.error("failed to execute browser function {}", name, e);
					return null;
				}
			}
		};
		browser.addDisposeListener(e -> {
			if (!func.isDisposed()) {
				func.dispose();
			}
		});
	}

	public static Font boldFont() {
		return JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);
	}

	public static Font italicFont() {
		return JFaceResources.getFontRegistry().getItalic(
				JFaceResources.DEFAULT_FONT);
	}

	public static Font defaultFont() {
		return JFaceResources.getFontRegistry().defaultFont();
	}

	/**
	 * Creates an italic font using the font data of the given control. The returned
	 * font must be disposed by the respective caller.
	 */
	public static Font italicFont(Control control) {
		if (control == null)
			return null;
		FontData fd = control.getFont().getFontData()[0];
		fd.setStyle(SWT.ITALIC);
		return new Font(control.getDisplay(), fd);
	}

	public static void applyItalicFont(Control control) {
		control.setFont(italicFont(control));
	}

	public static void center(Shell parent, Shell child) {
		Rectangle shellBounds = parent.getBounds();
		Point size = child.getSize();
		int diffX = (shellBounds.width - size.x) / 2;
		int diffY = (shellBounds.height - size.y) / 2;
		child.setLocation(shellBounds.x + diffX, shellBounds.y + diffY);
	}

	public static void adapt(FormToolkit toolkit, Composite composite) {
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
	}

	public static GridData gridData(
			Control control, boolean hFill, boolean vFill) {
		int hStyle = hFill ? SWT.FILL : SWT.LEFT;
		int vStyle = vFill ? SWT.FILL : SWT.CENTER;
		GridData data = new GridData(hStyle, vStyle, hFill, vFill);
		control.setLayoutData(data);
		return data;
	}

	public static GridData gridWidth(Control control, int width) {
		GridData data = gridData(control, false, false);
		data.widthHint = width;
		return data;
	}

	/**
	 * Creates a nice form header with the given title and returns the form.
	 */
	public static ScrolledForm formHeader(IManagedForm mForm, String title) {
		var form = mForm.getForm();
		var tk = mForm.getToolkit();
		tk.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText(title);
		return form;
	}

	public static Composite formSection(
			Composite parent, FormToolkit tk, String label) {
		var section = section(parent, tk, label);
		return sectionClient(section, tk);
	}

	public static Section section(
			Composite parent, FormToolkit tk, String label) {
		return makeSection(parent, tk, label, ExpandableComposite.EXPANDED);
	}

	public static Section collapsedSection(
			Composite parent, FormToolkit tk, String label) {
		return makeSection(parent, tk, label, ExpandableComposite.COMPACT);
	}

	private static Section makeSection(
			Composite parent, FormToolkit tk, String label, int expansion) {
		var s = tk.createSection(parent,
				ExpandableComposite.SHORT_TITLE_BAR
						| ExpandableComposite.FOCUS_TITLE
						| ExpandableComposite.TWISTIE
						| expansion
		);
		gridData(s, true, false);
		s.setText(label);
		return s;
	}

	/**
	 * Creates a composite and sets it as section client of the given section. The
	 * created composite gets a 2-column grid-layout.
	 */
	public static Composite sectionClient(Section section, FormToolkit tk) {
		var composite = tk.createComposite(section);
		section.setClient(composite);
		gridLayout(composite, 2);
		return composite;
	}

	public static Composite formBody(ScrolledForm form, FormToolkit toolkit) {
		Composite body = form.getBody();
		GridLayout bodyLayout = new GridLayout();
		bodyLayout.marginRight = 10;
		bodyLayout.marginLeft = 10;
		bodyLayout.horizontalSpacing = 10;
		bodyLayout.marginBottom = 10;
		bodyLayout.marginTop = 10;
		bodyLayout.verticalSpacing = 10;
		bodyLayout.numColumns = 1;
		body.setLayout(bodyLayout);
		toolkit.paintBordersFor(body);
		gridData(body, true, true);
		return body;
	}

	public static GridLayout gridLayout(Composite composite, int columns) {
		return gridLayout(composite, columns, 10, 10);
	}

	public static GridLayout innerGrid(Composite composite, int columns) {
		return gridLayout(composite, columns, 5, 0);
	}

	public static GridLayout gridLayout(Composite composite, int columns,
																			int spacing, int margin) {
		final GridLayout layout = new GridLayout(columns, false);
		layout.verticalSpacing = spacing;
		layout.marginWidth = margin;
		layout.marginHeight = margin;
		layout.horizontalSpacing = spacing;
		composite.setLayout(layout);
		return layout;
	}

	public static void fillHorizontal(Control control) {
		gridData(control, true, false);
	}

	public static Composite formComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		gridLayout(composite, 2);
		return composite;
	}

	public static Composite formComposite(Composite parent,
																				FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		gridLayout(composite, 2);
		return composite;
	}

	public static Button formCheckBox(Composite parent, String label) {
		return formCheckBox(parent, null, label);
	}

	public static Button formCheckBox(
			Composite parent, FormToolkit tk, String label) {
		formLabel(parent, label);
		var button = tk != null
				? tk.createButton(parent, null, SWT.CHECK)
				: new Button(parent, SWT.CHECK);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		button.setLayoutData(gd);
		return button;
	}

	public static Text formText(Composite parent, int flags) {
		return formText(parent, null, null, flags | SWT.BORDER);
	}

	public static Text formText(Composite parent, String label) {
		return formText(parent, null, label);
	}

	public static Text formText(Composite parent, String label, int flags) {
		return formText(parent, null, label, flags | SWT.BORDER);
	}

	public static Text formText(Composite parent, FormToolkit toolkit,
															String label) {
		return formText(parent, toolkit, label, SWT.BORDER);
	}

	public static Text formText(
			Composite parent, FormToolkit tk, String label, int flags) {
		if (label != null)
			formLabel(parent, tk, label);
		var text = tk != null
				? tk.createText(parent, null, flags)
				: new Text(parent, flags);
		gridData(text, true, false);
		return text;
	}

	public static Text formMultiText(Composite parent, String label) {
		return formMultiText(parent, null, label);
	}

	public static Text formMultiText(
			Composite parent, FormToolkit tk, String label) {
		formLabel(parent, tk, label);
		int style = SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI;
		var text = tk != null
				? tk.createText(parent, null, style)
				: new Text(parent, style);
		var gd = gridData(text, true, false);
		gd.minimumHeight = 50;
		gd.heightHint = 50;
		gd.widthHint = 100;
		return text;
	}

	public static Combo formCombo(Composite parent, String label) {
		return formCombo(parent, null, label);
	}

	public static Combo formCombo(
			Composite parent, FormToolkit tk, String label) {
		formLabel(parent, tk, label);
		var combo = new Combo(parent, SWT.READ_ONLY);
		gridData(combo, true, false);
		return combo;
	}

	public static Label formLabel(Composite parent, String text) {
		return formLabel(parent, null, text);
	}

	public static Label formLabel(Composite parent, FormToolkit toolkit,
																String label) {
		Label labelWidget;
		if (toolkit != null)
			labelWidget = toolkit.createLabel(parent, label, SWT.NONE);
		else {
			labelWidget = new Label(parent, SWT.NONE);
			labelWidget.setText(label);
		}
		GridData gridData = gridData(labelWidget, false, false);
		gridData.verticalAlignment = SWT.TOP;
		gridData.verticalIndent = 2;
		return labelWidget;
	}

	public static Label filler(Composite parent, FormToolkit tk) {
		return formLabel(parent, tk, "");
	}

	public static Label filler(Composite parent) {
		return formLabel(parent, "");
	}

}
