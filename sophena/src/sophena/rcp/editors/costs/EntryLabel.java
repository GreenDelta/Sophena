package sophena.rcp.editors.costs;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.rcp.Images;
import sophena.utils.Num;

class EntryLabel extends LabelProvider implements ITableLabelProvider {

	private final ProductType type;

	EntryLabel(ProductType type) {
		this.type = type;
	}

	@Override
	public Image getColumnImage(Object obj, int col) {
		return col == 0 ? Images.getImage(type) : null;
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof ProductEntry e)) return null;
		ProductCosts c = e.costs;
		if (c == null) return null;
		return switch (col) {
			case 0 -> e.product != null ? e.product.name : null;
			case 1 -> Num.str(e.count) + " Stück";
			case 2 -> Num.str(c.investment) + " EUR";
			case 3 -> Num.intStr(c.duration) + " a";
			case 4 -> Num.str(c.repair) + " %";
			case 5 -> Num.str(c.maintenance) + " %";
			case 6 -> Num.str(c.operation) + " h/a";
			default -> null;
		};
	}
}
