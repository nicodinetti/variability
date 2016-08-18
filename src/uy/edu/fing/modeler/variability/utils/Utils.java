package uy.edu.fing.modeler.variability.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import uy.edu.fing.modeler.variability.core.ReemplazadorMain;
import uy.edu.fing.modeler.variability.log.LogUtils;

public class Utils {

	public static List<String> tags = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask",
			"bpmn2:receiveTask", "bpmn2:startEvent", "bpmn2:endEvent", "bpmn2:sequenceFlow", "bpmn2:subProcess", "bpmn2:process", "bpmndi:BPMNDiagram", "bpmndi:BPMNShape", "bpmndi:BPMNEdge",
			"bpmn2:inclusiveGateway", "bpmn2:exclusiveGateway", "bpmn2:intermediateCatchEvent");
	public static List<String> tasks = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask",
			"bpmn2:receiveTask", "bpmn2:intermediateCatchEvent");

	public static List<String> excludedTags = Arrays.asList("bpmn2:process", "bpmndi:BPMNDiagram");

	private static final boolean PRINT_LOGS = ReemplazadorMain.PRINT_LOGS;
	private static int RENAME_SALT_NUMBER = 0;
	private static String RENAME_SALT = "_0";

	public static Document getDocument(String path, String processFileName) throws ParserConfigurationException, SAXException, IOException {
		Path filepathBase = Paths.get(path + File.separatorChar + processFileName);
		DocumentBuilder docBuilder = getDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());
		return doc;
	}

	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder;
	}

	public static List<Node> getSubTree(Document doc) {
		RENAME_SALT_NUMBER++;
		RENAME_SALT = "_" + RENAME_SALT_NUMBER;
		System.out.println("--- ARMADO DEL ARBOL A EXPORTAR ---");

		Node start = getStartEvent(doc);
		ArrayList<String> visiteds = new ArrayList<>();
		visiteds.add(getTAGID(start));
		List<Node> result = getSubTreeImpl(doc, start, visiteds);
		return result;
	}

	public static List<Node> getSubTreeImpl(Document doc, Node node, List<String> visiteds) {
		System.out.println("----> " + visiteds);
		List<Node> res = new ArrayList<>();

		Node nodeChanged = changeNodeID(node.cloneNode(true));
		res.add(nodeChanged);

		List<Node> nextNodes = getNextNode(doc, node, visiteds);

		for (Node next : nextNodes) {
			visiteds.add(getTAGID(next));
		}

		for (Node next : nextNodes) {
			List<Node> rec = getSubTreeImpl(doc, next, visiteds);
			res.addAll(rec);
		}
		return res;
	}

	/*
	 * VIEJO public static List<Node> getSubTree(Document doc) { Node nodo =
	 * getStartEvent(doc); System.out.println(
	 * "--- ARMADO DEL ARBOL A EXPORTAR ---"); List<Node> result = new
	 * ArrayList<Node>(); Node aInsertar = null; Node aux = getNextNode(doc,
	 * nodo); while (!getNodeTAG(doc, getTAGID(aux)).equals("bpmn2:endEvent")) {
	 * aInsertar = aux.cloneNode(true); result.add(changeNodeID(doc,
	 * aInsertar)); aux = getNextNode(doc, aux); } return result; }
	 */

	public static Node getStartEvent(Document doc) {
		return doc.getElementsByTagName("bpmn2:startEvent").item(0);
	}

	public static Node getNodeByID(Document doc, String nodoID) {

		for (String tag : tags) {
			Node aux = getNodeByID(doc, tag, nodoID);
			if (aux != null) {
				return aux;
			}
		}
		return null;

	}

	public static String getNodeTAG(Document doc, String nodoID) {

		Node aux = getNodeByID(doc, nodoID);
		if (aux != null) {
			return aux.getNodeName();
		}

		return null;
	}

	public static String getTAGID(Node node) {
		return ((Element) node).getAttribute("id");
	}

	public static String getTAGName(Node node) {
		return ((Element) node).getAttribute("name");
	}

	public static List<Node> getNextNode(Document doc2, Node first, List<String> visiteds) {
		List<Node> res = new ArrayList<>();

		String id = getTAGID(first);
		if (isEndEvent(getNodeTAG(doc2, id))) {
			return res;
		}

		if (isSequenceFlow(getNodeTAG(doc2, id))) {
			String outgoingName = ((Element) first).getAttribute("targetRef");
			if (!visiteds.contains(outgoingName)) {
				Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
				res.add(outgoingFlowNode);
			}
			return res;
		}

		List<String> outgoings = getNodeFlowID(first, "bpmn2:outgoing");
		for (String outgoing : outgoings) {
			if (!visiteds.contains(outgoing)) {
				Node outgoingFlowNode = getNodeByID(doc2, outgoing);
				res.add(outgoingFlowNode);
			}
		}
		return res;
	}

	public static List<String> getNodeFlowID(Node vPNode, String flowType) {
		List<String> res = new ArrayList<>();
		NodeList childNodes = vPNode.getChildNodes(); // vPNode = Task_1 nodo
		int length = childNodes.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = childNodes.item(it);
			String vPTagName = nodo.getNodeName();
			if (vPTagName.equals(flowType)) {
				res.add(nodo.getTextContent());
			}
		}
		return res;
	}

	public static Node getNodeByTag(Document doc, String tag, String idTarget) {
		NodeList tagsList;
		int length;
		tagsList = doc.getElementsByTagName(tag);
		length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node node = tagsList.item(it);
			if ((((Element) node).getAttribute("bpmnElement")).equals(idTarget)) {
				if (PRINT_LOGS)
					LogUtils.log("getNodeByTag", "Nodo encontrado " + getTAGID(node));
				return node;
			}
		}

		if (PRINT_LOGS)
			LogUtils.log("getNodeByTag", "Nodo no encontrado " + idTarget);
		return null;
	}

	private static Node getNodeByID(Document doc, String tag, String idTarget) {

		// System.out.println("************Buscando " + tag + " con ID= " +
		// idTarget);
		NodeList tagsList = doc.getElementsByTagName(tag);
		int length = tagsList.getLength();
		for (int it = 0; it < length; it++) {
			Node node = tagsList.item(it);
			String tagid = getTAGID(node);
			if (tagid.equals(idTarget)) {
				/*
				 * if (PRINT_LOGS) LogUtils.log("getTAGNodeByID",
				 * "Nodo encontrado " + getTAGID(node));
				 */
				// System.out.println("********************************
				// Encontrado encontrado " + tag + " con ID= " + idTarget);
				return node;
			}
		}

		// LogUtils.log("getTAGNodeByID", "Nodo No encontrado " + idTarget);
		return null;
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

	public static boolean isTask(String tag) {
		return tasks.contains(tag);
	}

	public static boolean isStartEvent(String tag) {
		return tag.equals("bpmn2:startEvent");
	}

	public static boolean isEndEvent(String tag) {
		return tag.equals("bpmn2:endEvent");
	}

	public static boolean isSequenceFlow(String tag) {
		return tag.equals("bpmn2:sequenceFlow");
	}

	public static boolean isGateway(String tag) {
		return Arrays.asList("bpmn2:inclusiveGateway", "bpmn2:exclusiveGateway").contains(tag);
	}

	public static void setFlowNode(Node source, String flowID, String tag) {
		Node sourceChild = ((Element) source).getElementsByTagName(tag).item(0);
		sourceChild.setTextContent(flowID);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + flowID + " *-----*");
		}

	}

	public static void setFlowRef(Node source, String refID, String tag) {
		((Element) source).setAttribute(tag, refID);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + refID + " *-----*");
		}
	}

	public static Node changeNodeID(Node nodo) {
		String nuevoNodoID = getTAGID(nodo) + RENAME_SALT;
		System.out.println("nuevoNodoID = " + nuevoNodoID);
		((Element) nodo).setAttribute("id", nuevoNodoID);
		if (isSequenceFlow(nodo.getNodeName())) {
			// Si el nodo es SequenceFlow, también le agrego un SALT al
			// sourceRef y targetRef
			((Element) nodo).setAttribute("sourceRef", ((Element) nodo).getAttribute("sourceRef") + RENAME_SALT);
			((Element) nodo).setAttribute("targetRef", ((Element) nodo).getAttribute("targetRef") + RENAME_SALT);
			System.out.println("isSequenceFlow");

		} else {

			NodeList inc = ((Element) nodo).getElementsByTagName("bpmn2:incoming");
			for (int i = 0; i < inc.getLength(); i++) {
				Node item = inc.item(i);
				String id = item.getTextContent();
				item.setTextContent(id + RENAME_SALT);
				System.out.println("incoming=" + id + RENAME_SALT);
			}

			NodeList out = ((Element) nodo).getElementsByTagName("bpmn2:outgoing");
			for (int i = 0; i < out.getLength(); i++) {
				Node item = out.item(i);
				String id = item.getTextContent();
				item.setTextContent(id + RENAME_SALT);
				System.out.println("outgoing=" + id + RENAME_SALT);
			}

		}
		return nodo;
	}

	public static Document insertSubTree(Document doc, Node parentNode, List<Node> nodos) {
		LogUtils.log("insertSubTree", "Insertando nodos en el nuevo documento: " + nodos);
		printTree(nodos);

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

	public static void changeBPMNEdgeTarget(Document doc, String elementId, String newRefElement, String ref) {
		NodeList bpmnEdges = doc.getElementsByTagName("bpmndi:BPMNEdge");
		int length = bpmnEdges.getLength();
		for (int it = 0; it < length; it++) {
			Node nodo = bpmnEdges.item(it);
			if (getTAGID(nodo).equals(elementId)) {
				((Element) nodo).setAttribute(ref, newRefElement);
				if (PRINT_LOGS)
					LogUtils.log("changeBPMNEdgeTarget", "Cambiado " + getTAGID(nodo));
				break;
			}
		}
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

	public static void printTree(List<Node> nodos) {
		for (Node node : nodos) {
			LogUtils.log("TREE: getTAGID", getTAGID(node));
		}
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
			if (PRINT_LOGS)
				LogUtils.log("figureSupression", "Eliminado " + getTAGID(nodeShape) + " del padre " + getTAGID(nodePadre));
		}

	}

	public static List<Node> removeFirstAndLastNode(List<Node> lista) {
		List<Node> result = new ArrayList<Node>();
		int tamano = lista.size();
		for (int i = 1; i < (tamano - 1); i++) {
			result.add(lista.get(i));
		}
		return result;
	}

	public static void removeBPMNDiagram(Document doc) {
		Node nodo = doc.getElementsByTagName("bpmndi:BPMNDiagram").item(0);
		if (nodo != null) {
			Utils.deleteNode(nodo);
			if (PRINT_LOGS)
				LogUtils.log("removeBPMNDiagram", "Eliminado nodo " + getTAGID(nodo));
		}
	}

	public static void deleteNode(Node nodo) {
		Node nodoPadre = nodo.getParentNode();
		nodoPadre.removeChild(nodo);
		if (PRINT_LOGS)
			LogUtils.log("deleteNode", "Eliminado nodo " + getTAGID(nodo));
	}

	public static Document insertExtras(Document doc, Document tmp) {

		Node parentNode = doc.getElementsByTagName("bpmn2:definitions").item(0);

		Node item = tmp.getElementsByTagName("bpmn2:definitions").item(0);
		NodeList childNodes = item.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node item2 = childNodes.item(j);
			String tagName = item2.getNodeName();
			if (!excludedTags.contains(tagName)) {
				// Copiar el nodo a doc
				Node newNode = doc.importNode(item2, true);
				parentNode.appendChild(newNode);
				System.out.println("AGREGADO " + tagName);
			}
		}

		return doc;

	}

}
