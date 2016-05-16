package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ActivitySupression {
	
	public static void activitySupression(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());
		List<Node> vPNodes = Utils.getVPListByType(doc, "bpmn2:task", "VPTask");
		vPNodes.addAll(Utils.getVPListByType(doc, "bpmn2:subProcess", "VPSubProcess"));
		
		for (Node vPNode : vPNodes) {
			String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();							// vPID = Task_1
																					// =
																					// Task_1
			String selectedVariant = selectedVariants.get(vPID);
			if (ReemplazadorMain.DELETE.equals(selectedVariant)) {
				String incomingFlow = Utils.getNodeFlowID(vPNode, "bpmn2:incoming");								// incomingFlow = SequenceFlow_1
				String outgoingFlow = Utils.getNodeFlowID(vPNode, "bpmn2:outgoing");								// outgoingFlow = SequenceFlow_4
				
				Node incomingNode = Utils.getTAGNodeByID(doc, "bpmn2:sequenceFlow", incomingFlow);				// incomingNode = SequenceFlow_1 nodo
				Node outgoingNode = Utils.getTAGNodeByID(doc, "bpmn2:sequenceFlow", outgoingFlow);				// outgoingNode = SequenceFlow_4 nodo
				
				String targetRefFinal = ((Element) outgoingNode).getAttribute("targetRef");					// targetRefFinal = Task_2
				((Element) incomingNode).setAttribute("targetRef", targetRefFinal);
				
				Node targetRefFinalNode = Utils.getTargetRefFinalNode(doc, targetRefFinal);
				
				Node targetRefFinalNodeIncoming = ((Element) targetRefFinalNode).getElementsByTagName("bpmn2:incoming").item(0);
				targetRefFinalNodeIncoming.setTextContent(Utils.getTAGID(incomingNode));
				
				Node outgoingFlowNodeEdge = 	Utils.getNodeByTag(doc, "bpmndi:BPMNEdge", outgoingFlow);
				Node incomingFlowNodeEdge = 	Utils.getNodeByTag(doc, "bpmndi:BPMNEdge", incomingFlow);
				Node vPIDNodeShape = 			Utils.getNodeByTag(doc, "bpmndi:BPMNShape", vPID);
				Node targetRefFinalNodeShape = 	Utils.getNodeByTag(doc, "bpmndi:BPMNShape", targetRefFinal);
				
				// Eliminacion del FIGURE a borrar
				Utils.figureSupression(doc, Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");
				Utils.figureSupression(doc, Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");
				Utils.figureSupression(doc, Utils.getTAGID(outgoingFlowNodeEdge), "bpmndi:BPMNEdge");
				Utils.changeBPMNEdgeTarget(doc, Utils.getTAGID(incomingFlowNodeEdge), Utils.getTAGID(targetRefFinalNodeShape), "targetElement");
				
				// Eliminacion del TAG a borrar
				Node nodoPadre = vPNode.getParentNode();
				nodoPadre.removeChild(vPNode); // vPNode = Task_1 nodo

				nodoPadre = outgoingNode.getParentNode();
				nodoPadre.removeChild(outgoingNode); // outgoingNode =
														// SequenceFlow_4 nodo
			}
		}
		Utils.saveResult(doc, basePath, resultFileName);
	}
	
}
