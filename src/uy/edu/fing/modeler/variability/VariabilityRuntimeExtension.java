package uy.edu.fing.modeler.variability;

import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
import org.eclipse.bpmn2.modeler.core.LifecycleEvent;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil.Bpmn2DiagramType;
import org.eclipse.bpmn2.modeler.ui.DefaultBpmn2RuntimeExtension.RootElementParser;
import org.eclipse.bpmn2.modeler.ui.wizards.FileService;
import org.eclipse.ui.IEditorInput;
import org.xml.sax.InputSource;

public class VariabilityRuntimeExtension implements IBpmn2RuntimeExtension {

	private static final String BPMN2_VARIABILITY = "http://www.fing.edu.uy/bpmn2-variability";

	@Override
	public String getTargetNamespace(Bpmn2DiagramType arg0) {
		return BPMN2_VARIABILITY;
	}

	// test namespaces of bpmn content.
	@Override
	public boolean isContentForRuntime(IEditorInput input) {
		InputSource source = new InputSource(FileService.getInputContents(input));
		RootElementParser parser = new RootElementParser(BPMN2_VARIABILITY);
		parser.parse(source);
		return parser.getResult();
	}

	@Override
	public void notify(LifecycleEvent event) {
		// TODO Auto-generated method stub

	}

}