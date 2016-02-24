package uy.edu.fing.modeler.variability.model.variant;

import org.eclipse.bpmn2.impl.ActivityImpl;

public class VActivityImpl extends ActivityImpl implements VActivity {

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
