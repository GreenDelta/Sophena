package sophena.calc;

import java.io.StringWriter;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalcLog {

	private static final int PAGE_WIDTH = 80;

	private final StringWriter writer;

	public CalcLog(String title) {
		writer = new StringWriter();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		String date = sdf.format(new Date());
		println(right(date));
		h1(title);
	}

	public void h1(String title) {
		println();
		println(center(title));
		println(center(repeat('=', len(title))));
		println();
	}

	public void h2(String title) {
		println();
		println(center(title));
		println(center(repeat('-', len(title))));
		println();
	}

	public void h3(String title) {
		println();
		println(center(title));
		println();
	}

	private String center(String s) {
		String text = strip(s);
		int padding = (PAGE_WIDTH - text.length()) / 2;
		if (padding <= 0)
			return text;
		return repeat(' ', padding) + text;
	}

	private String right(String s) {
		String text = strip(s);
		int offset = PAGE_WIDTH - text.length();
		return repeat(' ', offset) + text;
	}

	public void println() {
		writer.append('\n');
	}

	public void println(String s) {
		if (s == null)
			writer.append('\n');
		writer.write(s);
		writer.append('\n');
	}

	private String strip(String s) {
		if (s == null)
			return "";
		String t = s.trim();
		if (t.length() > PAGE_WIDTH)
			t = t.substring(0, PAGE_WIDTH - 3) + "...";
		return t;
	}

	private int len(String s) {
		if (s == null)
			return 0;
		int len = s.trim().length();
		return len < PAGE_WIDTH ? len : PAGE_WIDTH;
	}

	private String repeat(char c, int n) {
		return CharBuffer.allocate(n).toString().replace('\0', c);
	}

	@Override
	public String toString() {
		return writer.toString();
	}

}
