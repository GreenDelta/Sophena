package sophena.rcp.utils;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Text;

public final class Texts {

	private Texts() {
	}

	public static void onInt(Text text, Consumer<Integer> fn) {
		if (text == null || fn == null)
			return;
		text.addModifyListener((e) -> {
			try {
				String t = text.getText().trim();
				if (t.isEmpty())
					fn.accept(null);
				else
					fn.accept(Integer.parseInt(t, 10));
				text.setBackground(Colors.getWhite());
				text.setToolTipText(null);
			} catch (Exception ex) {
				text.setBackground(Colors.getErrorColor());
				text.setToolTipText("#not an integer");
			}
		});
	}
}
