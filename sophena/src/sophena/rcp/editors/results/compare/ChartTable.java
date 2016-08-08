package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class ChartTable {

	private Comparison comparison;
	private Data data;

	private ChartTable(Comparison comparison, Data data) {
		this.comparison = comparison;
		this.data = data;
	}

	static void create(Comparison comparison, Composite comp, Data data) {
		new ChartTable(comparison, data).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Projekt",
				data.columnLabel());
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1);
		Tables.autoSizeColumns(table);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < comparison.projects.length; i++) {
			Project project = comparison.projects[i];
			ProjectResult result = comparison.results[i];
			items.add(Item.create(i, project,
					data.value(result.costResult)));
			items.add(Item.createFunding(i, project,
					data.value(result.costResultFunding)));
		}
		return items;
	}

	private static class Item {
		int i;
		String project;
		String value;
		boolean fundingTrace = false;

		static Item create(int i, Project project, double value) {
			Item item = new Item();
			item.i = i;
			item.project = project.name + " - ohne Förderung";
			item.value = Num.intStr(Math.round(value));
			return item;
		}

		static Item createFunding(int i, Project project, double value) {
			Item item = new Item();
			item.fundingTrace = true;
			item.i = i;
			item.project = project.name + " - mit Förderung";
			item.value = Num.intStr(Math.round(value));
			return item;
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		private ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Item) || col != 0)
				return null;
			Item item = (Item) obj;
			if (item.fundingTrace)
				return img.get(item.i, 100);
			else
				return img.get(item.i);
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			switch (col) {
			case 0:
				return item.project;
			case 1:
				return item.value;
			default:
				return null;
			}
		}

		@Override
		public void dispose() {
			img.dispose();
			super.dispose();
		}

	}

	interface Data {

		String columnLabel();

		double value(CostResult result);

	}

}
