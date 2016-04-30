package uy.edu.fing.modeler.variability.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

public class ViewAction3Delegate extends ActionDelegate implements IViewActionDelegate {

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		box.setMessage("Executing: " + getClass());
		box.open();
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		System.out.println("soy el " + ViewAction3Delegate.class);
	}

}
