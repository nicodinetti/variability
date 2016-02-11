package uy.edu.fing.modeler.variability.task;

import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.graphiti.features.IFeatureProvider;

public class VPTask extends CustomShapeFeatureContainer {

	@Override
	protected VPTaskElementFeatureContainer createFeatureContainer(IFeatureProvider fp) {
		return new VPTaskElementFeatureContainer();
	}

	// @Override
	// public IAddFeature getAddFeature(IFeatureProvider fp) {
	// return new AddTaskFeature(fp) {
	// @Override
	// protected void decorateShape(IAddContext context, ContainerShape
	// containerShape, Task businessObject) {
	// super.decorateShape(context, containerShape, businessObject);
	// Rectangle selectionRect = (Rectangle)
	// containerShape.getGraphicsAlgorithm();
	// int width = 160;
	// int height = 90;
	// selectionRect.setWidth(width);
	// selectionRect.setHeight(height);
	//
	// Task ta = BusinessObjectUtil.getFirstElementOfType(containerShape,
	// Task.class);
	// if (ta != null) {
	// Shape shape = containerShape.getChildren().get(0);
	// ShapeStyle ss = new ShapeStyle();
	// ss.setDefaultColors(new ColorConstant(144, 176, 224));
	// StyleUtil.applyStyle(shape.getGraphicsAlgorithm(), ta, ss);
	// }
	// }
	// };
	// }
}
