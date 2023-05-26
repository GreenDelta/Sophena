package sophena.rcp.editors.basedata;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import sophena.model.BaseDataEntity;
import sophena.rcp.Icon;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public abstract class BaseTableLabel extends LabelProvider
		implements ITableLabelProvider, IFontProvider, IColorProvider {

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (col != 0)
			return null;
		if (!(obj instanceof BaseDataEntity))
			return null;
		BaseDataEntity entity = (BaseDataEntity) obj;
		return entity.isProtected ? Icon.LOCK_16.img() : Icon.EDIT_16.img();
	}

	@Override
	public Font getFont(Object obj) {
		if (!(obj instanceof BaseDataEntity))
			return null;
		BaseDataEntity entity = (BaseDataEntity) obj;
		if (entity.isProtected)
			return UI.italicFont();
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

	@Override
	public Color getForeground(Object obj) {
		if (!(obj instanceof BaseDataEntity))
			return null;
		BaseDataEntity entity = (BaseDataEntity) obj;
		if (entity.isProtected)
			return Colors.getDarkGray();
		return null;
	}

}
