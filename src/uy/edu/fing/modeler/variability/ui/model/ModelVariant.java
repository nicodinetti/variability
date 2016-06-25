package uy.edu.fing.modeler.variability.ui.model;

import java.util.ArrayList;
import java.util.List;

public class ModelVariant {

	private String label;
	private String fileName;
	private String varpointName;
	private List<ModelVariant> modelVariants = new ArrayList<>();

	public String getVarpointName() {
		return varpointName;
	}

	public void setVarpointName(String varpointName) {
		this.varpointName = varpointName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String varpointName) {
		this.fileName = varpointName;
	}

	public List<ModelVariant> getModelVariants() {
		return modelVariants;
	}

	public void setModelVariants(List<ModelVariant> modelVariants) {
		this.modelVariants = modelVariants;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "ModelVariant [fileName=" + fileName + ", varpointName=" + varpointName + ", modelVariants=" + modelVariants + "]";
	}

}
