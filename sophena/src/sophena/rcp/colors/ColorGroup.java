package sophena.rcp.colors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import sophena.rcp.colors.ColorConfig.Group;
import sophena.rcp.utils.UI;

class ColorGroup {

	public static void create(Group group, Composite parent) {
		var g = new org.eclipse.swt.widgets.Group(parent, SWT.NONE);
		g.setText(group.label());
		UI.fillHorizontal(g);
		UI.gridLayout(g, 6).makeColumnsEqualWidth = true;
		ColorBox.of("Grundfarbe", g)
				.setColor(group.base())
				.onChange(group::setBase);
		for (int i = 0; i < 5; i++) {
			int idx = i;
			ColorBox.of("Variante " + (i + 1), g)
					.setColor(group.variant(i))
					.onChange(rgb -> group.setVariant(idx, rgb));
		}
	}

}
