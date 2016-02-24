package uy.edu.fing.modeler.variability.container;

import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.ShowPropertiesFeature;
import org.eclipse.bpmn2.modeler.core.model.ModelDecorator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.custom.ICustomFeature;

public class VariabilityFeatureContainer extends CustomShapeFeatureContainer {

	public VariabilityFeatureContainer() {

	}

	@Override
	public String getId(EObject object) {
		EStructuralFeature f = ModelDecorator.getAnyAttribute(object, "variability");
		if (f != null) {
			Object id = object.eGet(f);
			return id.toString();
		}

		return null;
	}

	@Override
	public ICustomFeature[] getCustomFeatures(IFeatureProvider fp) {
		return new ICustomFeature[] { new ShowPropertiesFeature(fp) };
	}
}