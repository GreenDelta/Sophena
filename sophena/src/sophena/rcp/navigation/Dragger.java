package sophena.rcp.navigation;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

public class Dragger extends CommonDragAdapterAssistant {

	@Override
	public Transfer[] getSupportedTransferTypes() {
		return new Transfer[] { TextTransfer.getInstance() };
	}

	@Override
	public boolean setDragData(DragSourceEvent event,
			IStructuredSelection selection) {
		return false;
	}

}
