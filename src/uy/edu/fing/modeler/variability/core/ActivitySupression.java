package uy.edu.fing.modeler.variability.core;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ActivitySupression {
	
	public static void activitySupression(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());
		List<Node> vPNodes = getVPListByType(doc, "bpmn2:task", "VPTask");
		vPNodes.addAll(getVPListByType(doc, "bpmn2:subProcess", "VPSubProcess"));
		
		for (Node vPNode : vPNodes) {
			String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();							// vPID = Task_1
			String selectedVariant = selectedVariants.get(vPID);
			if (ReemplazadorMain.DELETE.equals(selectedVariant)) {
				String incomingFlow = getNodeFlowID(vPNode, "bpmn2:incoming");								// incomingFlow = SequenceFlow_1
				String outgoingFlow = getNodeFlowID(vPNode, "bpmn2:outgoing");								// outgoingFlow = SequenceFlow_4
				
				Node incomingNode = getTAGNodeByID(doc, "bpmn2:sequenceFlow", incomingFlow);				// incomingNode = SequenceFlow_1 nodo
				Node outgoingNode = getTAGNodeByID(doc, "bpmn2:sequenceFlow", outgoingFlow);				// outgoingNode = SequenceFlow_4 nodo
				
				String targetRefFinal = ((Element) outgoingNode).getAttribute("targetRef");					// targetRefFinal = Task_2
				((Element) incomingNode).setAttribute("targetRef", targetRefFinal);
				
				Node targetRefFinalNode = getTargetRefFinalNode(doc, targetRefFinal);
				
				Node targetRefFinalNodeIncoming = ((Element) targetRefFinalNode).getElementsByTagName("bpmn2:incoming").item(0);
				targetRefFinalNodeIncoming.setTextContent(((Element) incomingNode).getAttribute("id"));
				
				Node outgoingFlowNodeEdge = 	getTAGNodeByBPMNElement(doc, "bpmndi:BPMNEdge", outgoingFlow);
				Node incomingFlowNodeEdge = 	getTAGNodeByBPMNElement(doc, "bpmndi:BPMNEdge", incomingFlow);
				Node vPIDNodeShape = 			getTAGNodeByBPMNElement(doc, "bpmndi:BPMNShape", vPID);
				Node targetRefFinalNodeShape = 	getTAGNodeByBPMNElement(doc, "bpmndi:BPMNShape", targetRefFinal);
				
				// Eliminacion del FIGURE a borrar
				figureSupression(basePath, doc, baseProcessFileName, (((Element) vPIDNodeShape).getAttribute("id")), "bpmndi:BPMNShape", resultFileName);
				figureSupression(basePath, doc, baseProcessFileName, (((Element) outgoingFlowNodeEdge).getAttribute("id")), "bpmndi:BPMNEdge", resultFileName);
				changeBPMNEdgeTarget(doc, baseProcessFileName, (((Element) incomingFlowNodeEdge).getAttribute("id")), (((Element) targetRefFinalNodeShape).getAttribute("id")), resultFileName);
				
				// Eliminacion del TAG a borrar
				Node nodoPadre = vPNode.getParentNode();
				nodoPadre.removeChild(vPNode);																// vPNode = Task_1 nodo
				
				nodoPadre = outgoingNode.getParentNode();
				nodoPadre.removeChild(outgoingNode);														// outgoingNode = SequenceFlow_4 nodo
			}
		}
		saveResult(doc, basePath, resultFileName);
	}
	
	public static String getNodeFlowID(Node vPNode, String flowType) {
		NodeList childNodes = vPNode.getChildNodes();														// vPNode = Task_1 nodo
		int length = childNodes.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = childNodes.item(it);
			String vPTagName = nodo.getNodeName();
			if (vPTagName.equals(flowType)) {
				return nodo.getTextContent();
			}
		}
		return null;
	}
	
	public static Node getTargetRefFinalNode(Document doc, String targetRefFinal) {
		if (getTAGNodeByID(doc, "bpmn2:task", targetRefFinal) != null) {									// Probamos si el nodo es del tipo TASK.
			return getTAGNodeByID(doc, "bpmn2:task", targetRefFinal);										// targetRefFinalNode = Task_2 nodo
		} else if (getTAGNodeByID(doc, "bpmn2:subProcess", targetRefFinal) != null) {						// Probamos si el nodo es del tipo SUBPROCESS.
			return getTAGNodeByID(doc, "bpmn2:subProcess", targetRefFinal);
		} else if (getTAGNodeByID(doc, "bpmn2:endEvent", targetRefFinal) != null) {							// Probamos si el nodo es del tipo ENDEVENT.
			return getTAGNodeByID(doc, "bpmn2:endEvent", targetRefFinal);
		} else {
			return null;
		}
	}

	public static Node getTAGNodeByBPMNElement(Document doc, String tag, String idTarget) {
		NodeList tagsList;
		int length;
		tagsList = doc.getElementsByTagName(tag);
		length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = tagsList.item(it);
			if ((((Element) nodo).getAttribute("bpmnElement")).equals(idTarget)) {
				return nodo;
			}
		}
		return null;
	}
	
	public static Node getTAGNodeByID(Document doc, String tag, String idTarget) {
		NodeList tagsList;
		int length;
		tagsList = doc.getElementsByTagName(tag);
		length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = tagsList.item(it);
			if ((((Element) nodo).getAttribute("id")).equals(idTarget)) {
				return nodo;
			}
		}
		return null;
	}
	
	public static void figureSupression(String basePath, Document doc, String baseProcessFileName, String elementId, String tag, String resultFileName) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		Node nodoShape = null;
		NodeList shapes = doc.getElementsByTagName(tag);
		int length = shapes.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = shapes.item(it);
			if ((((Element) nodo).getAttribute("id")).equals(elementId)) {
				nodoShape = nodo;
			}
		}
		
		if (nodoShape != null) {
			Node nodoPadre = nodoShape.getParentNode();
			nodoPadre.removeChild(nodoShape);
		}
		
		saveResult(doc, basePath, resultFileName);
	}
	
	public static void changeBPMNEdgeTarget(Document doc, String baseProcessFileName, String elementId, String newTargetElement, String resultFileName) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		Node elementNode = null;
		NodeList bpmnEdges = doc.getElementsByTagName("bpmndi:BPMNEdge");
		int length = bpmnEdges.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = bpmnEdges.item(it);
			String nodeID = ((Element) nodo).getAttribute("id");
			if (nodeID.equals(elementId)) {
				elementNode = nodo;																			// elementNode = BPMNEdge_SequenceFlow_1 nodo
			}
		}
		((Element) elementNode).setAttribute("targetElement", newTargetElement);
	}
	
	public static void saveResult(Document doc, String basePath, String fileName) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		Path filepathResult = Paths.get(basePath + File.separatorChar + fileName);
		StreamResult result = new StreamResult(new File(filepathResult.toString()));
		transformer.transform(source, result);
	}

	public static List<Node> getVPListByType(Document doc, String type, String variability) {
		NodeList nodeList = doc.getElementsByTagName(type);
		List<Node> variationPoints = new ArrayList<Node>();
		for (int it = 0; it < nodeList.getLength(); it++) {
			Node nodo = nodeList.item(it);
			NamedNodeMap attr = nodo.getAttributes();
			Node nodeAttr = attr.getNamedItem("ext:variability");
			if (nodeAttr != null && variability.equals(nodeAttr.getTextContent())) {
				variationPoints.add(nodo);
			}
		}
		return variationPoints;
	}

}
