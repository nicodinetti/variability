package uy.edu.fing.modeler.variability.ui.model;

import java.util.ArrayList;
import java.util.List;

public class Variant {

	private String varName;
	private List<VarPoint> vpOptions = new ArrayList<>();

	public String getVarName() {
		return varName;
	}

	public void setVarName(String name) {
		this.varName = name;
	}

	public List<VarPoint> getVpOptions() {
		return vpOptions;
	}

	public void setVpOptions(List<VarPoint> vpOptions) {
		this.vpOptions = vpOptions;
	}

	@Override
	public String toString() {
		return "VarOption [varName=" + varName + ", vpOptions=" + vpOptions + "]";
	}

}
