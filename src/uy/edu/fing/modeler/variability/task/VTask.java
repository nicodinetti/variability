package uy.edu.fing.modeler.variability.task;

import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.activity.task.AddTaskFeature;
import org.eclipse.bpmn2.modeler.core.preferences.ShapeStyle;
import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
import org.eclipse.bpmn2.modeler.core.utils.StyleUtil;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.util.ColorConstant;

public class VTask extends CustomShapeFeatureContainer {

	@Override
	protected VPTaskElementFeatureContainer createFeatureContainer(IFeatureProvider fp) {
		return new VPTaskElementFeatureContainer();
	}

	@Override
	public IAddFeature getAddFeature(IFeatureProvider fp) {
		return new AddTaskFeature(fp) {
			@Override
			protected void decorateShape(IAddContext context, ContainerShape containerShape, Task businessObject) {
				super.decorateShape(context, containerShape, businessObject);
				Rectangle selectionRect = (Rectangle) containerShape.getGraphicsAlgorithm();
				int width = 160;
				int height = 90;
				selectionRect.setWidth(width);
				selectionRect.setHeight(height);

				Task ta = BusinessObjectUtil.getFirstElementOfType(containerShape, Task.class);
				if (ta != null) {
					Shape shape = containerShape.getChildren().get(0);
					ShapeStyle ss = new ShapeStyle();
					ss.setDefaultColors(new ColorConstant(144, 176, 224));
					StyleUtil.applyStyle(shape.getGraphicsAlgorithm(), ta, ss);
				}
			}
		};
	}
}
