package sophena.rcp.utils;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import org.eclipse.swt.widgets.Text;
import sophena.rcp.Numbers;

public class DataBinding {

	private IEditor editor;

	public DataBinding() {
	}

	public DataBinding(IEditor editor) {
		this.editor = editor;
	}

	public void onDouble(Text text, DoubleSupplier getter, DoubleConsumer setter) {
		if (text == null || getter == null || setter == null)
			return;
		text.setText(Numbers.toString(getter.getAsDouble()));
		text.addModifyListener((e) -> {
			double val = Texts.getDouble(text);
			setter.accept(val);
			if(editor != null)
				editor.setDirty();
		});
	}

}
