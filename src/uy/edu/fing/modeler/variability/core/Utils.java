package uy.edu.fing.modeler.variability.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Utils {
	
	public static void setFlowNode(Node source, String flowID, String tag) {
		Node sourceChild = ((Element) source).getElementsByTagName(tag).item(0);
		sourceChild.setTextContent(flowID);
		System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + flowID + " *-----*");
	}
	
	public static void cambiarID(Node nodo) {
		String nuevoNodoID = getTAGID(nodo) + "_0";
		((Element) nodo).setAttribute("id", nuevoNodoID);
	}
	
	public static void setFlowRef(Document doc, Node source, String refID, String tag) {
		((Element) source).setAttribute(tag, refID);
		System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + refID + " *-----*");
	}
	
	public static List<Node> getSubTree(Document doc) {
		Node nodo = ActivitySupression.getTAGNodeByID(doc, "bpmn2:startEvent", ReemplazadorMain.START_EVENT_SUBPROCESS_ID);
		System.out.println("--- ARMADO DEL ARBOL A EXPORTAR ---");
		List<Node> result = new ArrayList<Node>();
		Node aux = getNextNode(doc, nodo);
		while (!getNodeTAG(doc, getTAGID(aux)).equals("bpmn2:endEvent")) {
			result.add(aux);
			aux = getNextNode(doc, aux);
		}
		return result;
	}
	
	public static List<Node> removerPrimerYUltimo(List<Node> lista) {
		List<Node> result = new ArrayList<Node>();
		int tamano = lista.size();
		for (int i = 1; i < (tamano - 1); i++) {
			result.add(lista.get(i));
		}
		return result;
	}
	
	public static Document insertSubTree(Document doc, Node first, List<Node> nodos) {
		System.out.println("\n--- INSERCIÓN DE LOS NODOS EN EL OTRO DOCUMENTO ---");
		Node parentNode = first.getParentNode();
		for (Node node : nodos) {
			Node newNode = doc.importNode(node, true);
			parentNode.appendChild(newNode);
			System.out.println("-------- INSERTAMOS: " + getTAGID(newNode));
		}
		System.out.println("--- FIN INSERCIÓN DE LOS NODOS EN EL OTRO DOCUMENTO ---\n");
		return doc;
	}
	
	public static void imprimirSF(Node node) {
		System.out.println("### IMPRESION DE Sequence Flow ###");
		System.out.println("# ID : " + ((Element) node).getAttribute("id"));
		System.out.println("# sourceRef : " + ((Element) node).getAttribute("sourceRef"));
		System.out.println("# targetRef : " + ((Element) node).getAttribute("targetRef"));
		System.out.println("### FIN ###");
	}
	
	public static Node getNodeByID(Document doc, String nodoID) {
		return ActivitySupression.getTAGNodeByID(doc, getNodeTAG(doc, nodoID), nodoID);
	}
	
	public static String getNodeTAG(Document doc, String nodoID) {
		Node aux = null;
		Node nodo = null;
		List<String> tags = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask", "bpmn2:receiveTask", "bpmn2:startEvent", "bpmn2:endEvent", "bpmn2:sequenceFlow", "bpmn2:subProcess", "bpmn2:process", "bpmndi:BPMNDiagram", "bpmndi:BPMNShape", "bpmndi:BPMNEdge");
		for (String tag : tags) {
			aux = ActivitySupression.getTAGNodeByID(doc, tag, nodoID);
			if (aux != null) {
				nodo = aux;
				break;
			}
		}
		return nodo.getNodeName();
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
			outgoingName = ActivitySupression.getNodeFlowID(first, "bpmn2:outgoing");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;			
		} else if (isSequenceFlow(getNodeTAG(doc2, getTAGID(first)))) {
			outgoingName = ((Element) first).getAttribute("targetRef");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;
		} else {
			outgoingName = ActivitySupression.getNodeFlowID(first, "bpmn2:outgoing");
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			nextNode = outgoingFlowNode;
		};
		return nextNode;
	}
	
	public static void deleteNode(Node nodo) {
		Node nodoPadre = nodo.getParentNode();
		nodoPadre.removeChild(nodo);
		System.out.println("*-----* ELIMINAMOS EL NODO " + getTAGID(nodo) + " *-----*");
		/*
		 * FALTA BORRAR LA PARTE VISUAL DEL NODO
		 */
	}

}
