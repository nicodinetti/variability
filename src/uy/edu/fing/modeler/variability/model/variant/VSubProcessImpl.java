package uy.edu.fing.modeler.variability.model.variant;

import org.eclipse.bpmn2.impl.SubProcessImpl;

public class VSubProcessImpl extends SubProcessImpl implements VSubProcess {

	private String vPointID;

	@Override
	public String getVPointID() {
		return vPointID;
	}

	@Override
	public void setVPointID(String vPointID) {
		this.vPointID = vPointID;
	}

}
