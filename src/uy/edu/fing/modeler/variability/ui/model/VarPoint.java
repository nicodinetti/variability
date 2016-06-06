package uy.edu.fing.modeler.variability.ui.model;

import java.util.ArrayList;
import java.util.List;

public class VarPoint {

	private String name;
	private List<Variant> varOptions = new ArrayList<>();

	public VarPoint(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Variant> getVarOptions() {
		return varOptions;
	}

	public void setVarOptions(List<Variant> varOptions) {
		this.varOptions = varOptions;
	}

	@Override
	public String toString() {
		return "VPOptions [name=" + name + ", varOptions=" + varOptions + "]";
	}

}
