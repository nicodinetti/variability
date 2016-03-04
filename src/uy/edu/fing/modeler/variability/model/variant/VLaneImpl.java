package uy.edu.fing.modeler.variability.model.variant;

import org.eclipse.bpmn2.impl.LaneImpl;

public class VLaneImpl extends LaneImpl implements VLane {

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
