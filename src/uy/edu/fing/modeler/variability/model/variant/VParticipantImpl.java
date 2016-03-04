package uy.edu.fing.modeler.variability.model.variant;

import org.eclipse.bpmn2.impl.ParticipantImpl;

public class VParticipantImpl extends ParticipantImpl implements VParticipant {

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
