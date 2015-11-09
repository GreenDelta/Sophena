package sophena.rcp.editors.basedata;

import java.util.List;

import sophena.db.usage.SearchResult;
import sophena.rcp.utils.MsgBox;

public class UsageError {

	private UsageError() {
	}

	public static void show(List<SearchResult> results) {
		String text = "Das ausgewählte Element kann nicht gelöscht werden, da "
				+ "es bereteits verwendet wird.";
		if (!results.isEmpty()) {
			SearchResult r = results.get(0);
			text += " Zum Beispiel in '" + r.name + "'.";
		}
		MsgBox.error("Kann nicht gelöscht werden", text);
	}

}
