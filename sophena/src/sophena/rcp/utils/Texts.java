package sophena.rcp.utils;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import sophena.utils.Num;

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

	public static void set(Text text, String value) {
		if (text == null || value == null)
			return;
		text.setText(value);
	}

	public static void set(Text text, double d) {
		if (text == null)
			return;
		text.setText(Num.str(d));
	}

	public static void set(Text text, Double d) {
		if (text == null)
			return;
		if (d == null)
			text.setText("");
		else
			text.setText(Num.str(d));
	}

	public static void set(Text text, int i) {
		if (text == null)
			return;
		text.setText(Num.intStr(i));
	}

	public static void set(Text text, Integer i) {
		if (text == null)
			return;
		if (i == null)
			text.setText("");
		else
			text.setText(Num.intStr(i));
	}

	public static boolean isEmpty(Text text) {
		if (text == null)
			return true;
		return Strings.nullOrEmpty(text.getText());
	}

	public static boolean hasNumber(Text text) {
		if (text == null)
			return false;
		Number n = Num.readNumber(text.getText());
		return n != null;
	}

	public static double getDouble(Text text) {
		if (text == null)
			return 0;
		else
			return Num.read(text.getText());
	}

	public static int getInt(Text text) {
		if (text == null)
			return 0;
		else
			return Num.readInt(text.getText());
	}

	public static boolean hasPercentage(Text text) {
		if (text == null)
			return false;
		Number n = Num.readNumber(text.getText());
		if (n == null)
			return false;
		double d = n.doubleValue();
		return 0 <= d && d <= 120;
	}

	public static TextDispatch on(Text text) {
		return new TextDispatch(text);
	}

	public static class TextDispatch {

		public final Text text;

		TextDispatch(Text text) {
			this.text = text;
		}

		public TextDispatch required() {
			if (text == null)
				return this;
			text.setBackground(Colors.forRequiredField());
			text.addModifyListener((e) -> {
				if (Strings.nullOrEmpty(text.getText()))
					text.setBackground(Colors.getErrorColor());
				else
					text.setBackground(Colors.forRequiredField());
			});
			return this;
		}

		public TextDispatch calculated() {
			if (text == null)
				return this;
			text.setBackground(Colors.forCalculatedField());
			text.setEditable(false);
			return this;
		}

		public TextDispatch decimal() {
			if (text == null)
				return this;
			text.addListener(SWT.Verify, (e) -> {
				if (e.text == null)
					return;
				for (char c : e.text.toCharArray()) {
					if (Character.isDigit(c))
						continue;
					if (c == '.' || c == ',' || c == '-' || c == '+'
							|| c == 'e' || c == 'E')
						continue;
					e.doit = false;
					return;
				}
			});
			return this;
		}

		public TextDispatch integer() {
			if (text == null)
				return this;
			text.addListener(SWT.Verify, (e) -> {
				if (e.text == null)
					return;
				for (char c : e.text.toCharArray()) {
					if (Character.isDigit(c))
						continue;
					// thousands separators
					if (c == '.' || c == ',' || c == '-' || c == '+')
						continue;
					e.doit = false;
					return;
				}
			});
			return this;
		}

		public TextDispatch validate(Runnable fn) {
			if (text == null || fn == null)
				return this;
			text.addModifyListener((e) -> fn.run());
			return this;
		}

		public TextDispatch onChanged(Consumer<String> fn) {
			if (text == null || fn == null)
				return this;
			text.addModifyListener((e) -> fn.accept(text.getText()));
			return this;
		}

		public TextDispatch readOnly() {
			if (text == null)
				return this;
			text.setEditable(false);
			return this;
		}

		public TextDispatch init(String s) {
			set(text, s);
			return this;
		}

		public TextDispatch init(int i) {
			set(text, i);
			return this;
		}

		public TextDispatch init(Integer i) {
			set(text, i);
			return this;
		}

		public TextDispatch init(double d) {
			set(text, d);
			return this;
		}

		public TextDispatch init(Double d) {
			set(text, d);
			return this;
		}

	}

}
