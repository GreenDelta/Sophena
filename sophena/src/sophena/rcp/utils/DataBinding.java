package sophena.rcp.utils;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.eclipse.swt.widgets.Text;
import sophena.rcp.Numbers;

public class DataBinding {

	private IEditor editor;

	public DataBinding() {
	}

	public DataBinding(IEditor editor) {
		this.editor = editor;
	}

	public void onDouble(Text text,
			Supplier<DoubleSupplier> getter,
			Supplier<DoubleConsumer> setter) {
		if (text == null || getter == null || setter == null)
			return;
		text.setText(Numbers.toString(getter.get().getAsDouble()));
		text.addModifyListener((e) -> {
			double val = Texts.getDouble(text);
			setter.get().accept(val);
			if(editor != null)
				editor.setDirty();
		});
	}

	public void onInt(Text text,
			Supplier<IntSupplier> getter,
			Supplier<IntConsumer> setter) {
		if (text == null || getter == null || setter == null)
			return;
		text.setText(Numbers.toString(getter.get().getAsInt()));
		text.addModifyListener((e) -> {
			int val = Texts.getInt(text);
			setter.get().accept(val);
			if(editor != null)
				editor.setDirty();
		});
	}

}
