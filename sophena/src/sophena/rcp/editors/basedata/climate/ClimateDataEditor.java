package sophena.rcp.editors.basedata.climate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ClimateDataEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("climate.data",
				M.ClimateData);
		Editors.open(input, "sophena.ClimateDataEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new TablePage(this));
			addPage(new MapPage(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}
