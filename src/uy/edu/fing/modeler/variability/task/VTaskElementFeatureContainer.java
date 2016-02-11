package uy.edu.fing.modeler.variability.task;

import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.ShowPropertiesFeature;
import org.eclipse.bpmn2.modeler.core.model.ModelDecorator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.custom.ICustomFeature;

public class VTaskElementFeatureContainer extends CustomShapeFeatureContainer {

	// these values must match what's in the plugin.xml
	private final static String TYPE = "VTask";
	private final static String ID = "VTask";

	public VTaskElementFeatureContainer() {

	}

	@Override
	public String getId(EObject object) {
		EStructuralFeature f = ModelDecorator.getAnyAttribute(object, "type");
		if (f != null) {
			Object id = object.eGet(f);
			if (TYPE.equals(id))
				return ID;
		}

		return null;
	}

	@Override
	public ICustomFeature[] getCustomFeatures(IFeatureProvider fp) {
		return new ICustomFeature[] { new ShowPropertiesFeature(fp) };
	}
}