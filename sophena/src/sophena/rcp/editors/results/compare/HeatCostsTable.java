package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import sophena.calc.Comparison;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class HeatCostsTable {

	private Comparison comparison;

	HeatCostsTable(Comparison comparison) {
		this.comparison = comparison;
	}

	void create(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Projekt",
				"Wärmegestehungskosten [EUR/MWh]");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Table t = table.getTable();
		t.getColumn(1).setAlignment(SWT.RIGHT);
		Tables.autoSizeColumns(t);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < comparison.projects.length; i++) {
			Project project = comparison.projects[i];
			ProjectResult result = comparison.results[i];
			items.add(Item.create(i, project, result));
			items.add(Item.createFunding(i, project, result));
		}
		return items;
	}

	private static class Item {
		int i;
		String project;
		String costs;
		boolean fundingTrace = false;

		static Item create(int i, Project project, ProjectResult result) {
			Item item = new Item();
			item.i = i;
			item.project = project.name + " - ohne Förderung";
			double costs = 1000 * result.costResult.grossTotal.heatGenerationCosts;
			item.costs = Long.toString(Math.round(costs));
			return item;
		}

		static Item createFunding(int i, Project project, ProjectResult result) {
			Item item = new Item();
			item.fundingTrace = true;
			item.i = i;
			item.project = project.name + " - mit Förderung";
			double costs = 1000 * result.costResultFunding.grossTotal.heatGenerationCosts;
			item.costs = Long.toString(Math.round(costs));
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
				return item.costs;
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

}
