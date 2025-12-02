package sophena.rcp.editors.basedata;

import sophena.model.AbstractProduct;

public class ProductTables {

	private ProductTables() {
	}

	public static String getText(AbstractProduct product, int col) {
		if (product == null)
			return null;
		switch (col) {
		case 0:
			return product.group != null ? product.group.name : null;
		case 1:
			return product.name;
		case 2:
			return product.productLine;
		case 3:
			return product.manufacturer != null ? product.manufacturer.name : null;
		default:
			return null;
		}
	}
}