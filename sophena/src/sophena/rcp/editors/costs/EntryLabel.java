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

	private ProductType type;

	EntryLabel(ProductType type) {
		this.type = type;
	}

	@Override
	public Image getColumnImage(Object obj, int col) {
		return col == 0 ? Images.getImage(type) : null;
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof ProductEntry))
			return null;
		ProductEntry e = (ProductEntry) obj;
		ProductCosts c = e.costs;
		if (c == null)
			return null;
		switch (col) {
		case 0:
			return e.product != null ? e.product.name : null;
		case 1:
			return Num.str(e.count) + " Stück";
		case 2:
			return Num.str(c.investment) + " EUR";
		case 3:
			return Num.intStr(c.duration) + " a";
		case 4:
			return Num.str(c.repair) + " %";
		case 5:
			return Num.str(c.maintenance) + " %";
		case 6:
			return Num.str(c.operation) + " h/a";
		default:
			return null;
		}
	}
}