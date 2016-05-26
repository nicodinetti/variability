package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import uy.edu.fing.modeler.variability.log.LogUtils;

public class Utils {

	private static final String RENAME_SALT = "_0";

	public static void setFlowNode(Node source, String flowID, String tag) {
		Node sourceChild = ((Element) source).getElementsByTagName(tag).item(0);
		sourceChild.setTextContent(flowID);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + flowID + " *-----*");
		}
	}

	public static Node changeNodeID(Node nodo) {
		String nuevoNodoID = getTAGID(nodo) + RENAME_SALT;
		((Element) nodo).setAttribute("id", nuevoNodoID);
		if (nodo.getNodeName().equals("bpmn2:sequenceFlow")) {
			// Si el nodo es SequenceFlow, también la agrego un SALT al
			// sourceRef y targetRef
			((Element) nodo).setAttribute("sourceRef", ((Element) nodo).getAttribute("sourceRef") + RENAME_SALT);
			((Element) nodo).setAttribute("targetRef", ((Element) nodo).getAttribute("targetRef") + RENAME_SALT);
		}
		return nodo;
	}

	public static void setFlowRef(Node source, String refID, String tag) {
		((Element) source).setAttribute(tag, refID);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + refID + " *-----*");
		}
	}

	public static List<Node> getSubTree(Document doc) {
		Node nodo = getStartEvent(doc);
		System.out.println("--- ARMADO DEL ARBOL A EXPORTAR ---");
		List<Node> result = new ArrayList<Node>();
		Node aInsertar = null;
		Node aux = getNextNode(doc, nodo);
		while (!getNodeTAG(doc, getTAGID(aux)).equals("bpmn2:endEvent")) {
			aInsertar = aux.cloneNode(true);
			result.add(changeNodeID(aInsertar));
			aux = getNextNode(doc, aux);
		}
		return result;
	}

	public static Node getStartEvent(Document doc) {
		return doc.getElementsByTagName("bpmn2:startEvent").item(0);
	}

	public static List<Node> removeFirstAndLastNode(List<Node> lista) {
		List<Node> result = new ArrayList<Node>();
		int tamano = lista.size();
		for (int i = 1; i < (tamano - 1); i++) {
			result.add(lista.get(i));
		}
		return result;
	}

	public static Document insertSubTree(Document doc, Node first, List<Node> nodos) {
		LogUtils.log("insertSubTree", "Insertando nodos en el nuevo documento: " + nodos);

		Node parentNode = first.getParentNode();
		for (Node node : nodos) {
			Node newNode = doc.importNode(node, true);
			parentNode.appendChild(newNode);
			if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
				LogUtils.logNext("insertSubTree", "Insertamos " + getTAGID(newNode));
				LogUtils.back();
			}
		}

		LogUtils.log("insertSubTree", "Fin Insertando nodos en el nuevo documento: " + nodos);
		return doc;
	}

	public static Node getNodeByID(Document doc, String nodoID) {
		return getTAGNodeByID(doc, getNodeTAG(doc, nodoID), nodoID);
	}

	public static String getNodeTAG(Document doc, String nodoID) {
		List<String> tags = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask", "bpmn2:receiveTask",
				"bpmn2:startEvent", "bpmn2:endEvent", "bpmn2:sequenceFlow", "bpmn2:subProcess", "bpmn2:process", "bpmndi:BPMNDiagram", "bpmndi:BPMNShape", "bpmndi:BPMNEdge");
		for (String tag : tags) {
			Node aux = getTAGNodeByID(doc, tag, nodoID);
			if (aux != null) {
				return aux.getNodeName();
			}
		}
		return null;
	}

	public static boolean isTask(String tag) {
		List<String> tags = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask", "bpmn2:receiveTask");
		return tags.contains(tag);
	}

	public static boolean isSequenceFlow(String tag) {
		List<String> tags = Arrays.asList("bpmn2:sequenceFlow");
		return tags.contains(tag);
	}

	public static String getTAGID(Node node) {
		return ((Element) node).getAttribute("id");
	}

	public static Node getNextNode(Document doc2, Node first) {
		Node nextNode = null;
		String outgoingName = "";
		if (isTask(getNodeTAG(doc2, getTAGID(first)))) {
			outgoingName = getNodeFlowID(first, "bpmn2:outgoing");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;
		} else if (isSequenceFlow(getNodeTAG(doc2, getTAGID(first)))) {
			outgoingName = ((Element) first).getAttribute("targetRef");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;
		} else {
			outgoingName = getNodeFlowID(first, "bpmn2:outgoing");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;
		}
		;
		return nextNode;
	}

	public static String getNodeFlowID(Node vPNode, String flowType) {
		NodeList childNodes = vPNode.getChildNodes(); // vPNode = Task_1 nodo
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
		List<String> types = Arrays.asList("bpmn2:task", "bpmn2:subProcess", "bpmn2:endEvent", "bpmn2:inclusiveGateway");
		for (String type : types) {
			Node node = getTAGNodeByID(doc, type, targetRefFinal);
			if (node != null) {
				LogUtils.log("getTargetRefFinalNode", "Nodo encontrado " + getTAGID(node));
				return node;
			}
		}

		throw new RuntimeException("Tipo de tarea destino no soportada");

	}

	public static Node getNodeByTag(Document doc, String tag, String idTarget) {
		NodeList tagsList;
		int length;
		tagsList = doc.getElementsByTagName(tag);
		length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node node = tagsList.item(it);
			if ((((Element) node).getAttribute("bpmnElement")).equals(idTarget)) {
				LogUtils.log("getNodeByTag", "Nodo encontrado " + getTAGID(node));
				return node;
			}
		}

		LogUtils.log("getNodeByTag", "Nodo no encontrado " + idTarget);
		return null;
	}

	public static Node getTAGNodeByID(Document doc, String tag, String idTarget) {
		NodeList tagsList;
		int length;
		tagsList = doc.getElementsByTagName(tag);
		length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node node = tagsList.item(it);
			if (getTAGID(node).equals(idTarget)) {
				LogUtils.log("getTAGNodeByID", "Nodo encontrado " + getTAGID(node));
				return node;
			}
		}

		LogUtils.log("getTAGNodeByID", "Nodo No encontrado " + idTarget);
		return null;
	}

	public static void figureSupression(Document doc, String elementId, String tag) {
		Node nodeShape = null;
		NodeList shapes = doc.getElementsByTagName(tag);
		int length = shapes.getLength();
		for (int it = 0; it < length; it++) {
			Node node = shapes.item(it);
			if (getTAGID(node).equals(elementId)) {
				nodeShape = node;
			}
		}

		if (nodeShape != null) {
			Node nodePadre = nodeShape.getParentNode();
			nodePadre.removeChild(nodeShape);
			LogUtils.log("figureSupression", "Eliminado " + getTAGID(nodeShape) + " del padre " + getTAGID(nodePadre));
		}

	}

	public static void changeBPMNEdgeTarget(Document doc, String elementId, String newRefElement, String ref) {
		NodeList bpmnEdges = doc.getElementsByTagName("bpmndi:BPMNEdge");
		int length = bpmnEdges.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = bpmnEdges.item(it);
			if (getTAGID(nodo).equals(elementId)) {
				((Element) nodo).setAttribute(ref, newRefElement);
				LogUtils.log("changeBPMNEdgeTarget", "Cambiado " + getTAGID(nodo));
				break;
			}
		}
	}

	public static void removeBPMNDiagram(Document doc) {
		Node nodo = doc.getElementsByTagName("bpmndi:BPMNDiagram").item(0);
		if (nodo != null) {
			Utils.deleteNode(nodo);
			LogUtils.log("removeBPMNDiagram", "Eliminado nodo " + getTAGID(nodo));
		}
	}

	public static void deleteNode(Node nodo) {
		Node nodoPadre = nodo.getParentNode();
		nodoPadre.removeChild(nodo);
		LogUtils.log("deleteNode", "Eliminado nodo " + getTAGID(nodo));
	}

	public static void saveResult(String baseProcessFileName, Document doc, String basePath, String fileName)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		Path filepathResult = Paths.get(basePath + File.separatorChar + fileName);
		StreamResult result = new StreamResult(new File(filepathResult.toString()));
		transformer.transform(source, result);
		LogUtils.log(baseProcessFileName, "Resultado guardado en: " + filepathResult);
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
