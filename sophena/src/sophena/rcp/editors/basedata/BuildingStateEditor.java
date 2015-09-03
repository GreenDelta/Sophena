package sophena.rcp.editors.basedata;



import java.util.List;



import org.eclipse.jface.viewers.ITableLabelProvider;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.IManagedForm;

import org.eclipse.ui.forms.editor.FormPage;

import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.eclipse.ui.forms.widgets.Section;



import sophena.db.daos.RootEntityDao;

import sophena.model.BuildingState;

import sophena.rcp.App;

import sophena.rcp.Images;

import sophena.rcp.Labels;

import sophena.rcp.Numbers;

import sophena.rcp.editors.Editor;

import sophena.rcp.utils.Editors;

import sophena.rcp.utils.KeyEditorInput;

import sophena.rcp.utils.Tables;

import sophena.rcp.utils.UI;



public class BuildingStateEditor extends Editor {



public static void open() {

KeyEditorInput input = new KeyEditorInput("data.building.states",

"Gebäudetypen");

Editors.open(input, "sophena.BuildingStateEditor");

}



@Override

protected void addPages() {

try {

addPage(new Page());

} catch (Exception e) {

log.error("failed to add page", e);

}

}



private class Page extends FormPage {



private RootEntityDao<BuildingState> dao;

private List<BuildingState> states;



public Page() {

super(BuildingStateEditor.this, "BuildingStatePage",

"Gebäudetypen");

dao = new RootEntityDao<>(BuildingState.class, App.getDb());

states = dao.getAll();

}



@Override

protected void createFormContent(IManagedForm managedForm) {

ScrolledForm form = UI.formHeader(managedForm, "Gebäudetypen");

FormToolkit toolkit = managedForm.getToolkit();

Composite body = UI.formBody(form, toolkit);

// TODO: create page content

createStateSection(body, toolkit);

form.reflow(true);

}


private void createStateSection(Composite parent,

FormToolkit toolkit) {

Section section = UI.section(parent, toolkit, "Gebäudetypen");

UI.gridData(section, true, true);

Composite comp = UI.sectionClient(section, toolkit);

UI.gridLayout(comp, 1);

TableViewer table = Tables.createViewer(comp, "Index",

"default", "Type", "HeatingLimit", "WaterFraction","Loadhours");

table.setLabelProvider(new BuildingStateLabel());

table.setInput(states);

Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2);

//bindBoilerActions(section, table);

}


private class BuildingStateLabel extends LabelProvider

implements ITableLabelProvider {



@Override

public Image getColumnImage(Object element, int col) {

return col == 0 ? Images.BOILER_16.img() : null;

}



@Override

public String getColumnText(Object element, int col) {

if (!(element instanceof BuildingState))

return null;

BuildingState buildingState = (BuildingState) element;

switch (col) {

case 0:

return buildingState.name;

case 1:

return getIndex(buildingState);

case 2:

return getIsdefault(buildingState);

case 3:

return getTypeLabel(buildingState);

case 4:

return getHeatingLimit(buildingState);

case 5:

return getWaterFraction(buildingState);

case 6:

return getLoadHours(buildingState);

default:

return null;

}

}


private String getIndex(BuildingState buildingState) {

if (buildingState == null)

return null;

else

return Numbers.toString(buildingState.index) ;

}



private String getIsdefault(BuildingState buildingState) {

if (buildingState == null)

return null;

else

if (buildingState.isDefault){

return "true";

}

else return "false";

}


private String getTypeLabel(BuildingState buildingState) {

if (buildingState.type != null)

return buildingState.type.name();

else

return Labels.get(buildingState.type);

}


private String getHeatingLimit(BuildingState buildingState) {

if (buildingState == null)

return null;

else

return Numbers.toString(buildingState.heatingLimit) ;

}


private String getWaterFraction(BuildingState buildingState) {

if (buildingState == null)

return null;

else

return Numbers.toString(buildingState.waterFraction) ;

}


private String getLoadHours(BuildingState buildingState) {

if (buildingState == null)

return null;

else  

return Numbers.toString(buildingState.loadHours) ;

}
}
}
}




                                                     
		