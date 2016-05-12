package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;


//http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.core.ActivitySubstitution;
import uy.edu.fing.modeler.variability.core.ActivitySupression;
import uy.edu.fing.modeler.variability.core.LaneSubstitution;

public class ReemplazadorMain {
	
	private static final String SUBPROCESS_ID = "Task_1";
	private static final String START_EVENT_SUBPROCESS_ID = "StartEvent_10";
	public static final String DELETE = "DELETE";
	private static boolean primero = true;
	private static String BASE_PATH = "";
	private static String BASE_PROCESS_FILE_NAME = "";
	private static String RESULT_FILE_NAME = "";
	/*
	private static final String BASE_PATH = "/home/abrusco/git/reemplazador/Reemplazador/src";
	private static final String BASE_PROCESS_FILE_NAME = "process_2.bpmn";
	private static final String RESULT_FILE_NAME = "result.bpmn";
	*/
	
	public static void main(String[] args) throws Exception {
		try {

			Map<String, String> selectedVariants = new HashMap<>();
			//selectedVariants.put("Task_1", "S1.bpmn");
			////selectedVariants.put("Task_1", "Lane_3");
			//selectedVariants.put("SubProcess_1", "T1.bpmn");
			
			/*String baseProcessFileName = "process_1.bpmn";
			//String baseProcessFileName = "SubProcess_1/C.bpmn";
			String resultFileName = "result.bpmn";
			Map<String, String> selectedVariants = new HashMap<>();
			selectedVariants.put("Task_1", "A.bpmn");*/
			//selectedVariants.put("SubProcess_1", "D.bpmn");
			
			/*
			 * Process 1
			selectedVariants.put("Task_1", DELETE);
			selectedVariants.put("SubProcess_1", DELETE);
			*/
			selectedVariants.put(SUBPROCESS_ID, "S2.bpmn");
			//selectedVariants.put("Task_2", DELETE);
			
			String basePath = "/home/abrusco/git/reemplazador/Reemplazador/src";
			String baseProcessFileName = "process_2.bpmn";
			String resultFileName = "result.bpmn";
			
			replace(basePath, baseProcessFileName, selectedVariants, resultFileName);
			
			/*
			ActivitySubstitution.activitySubstitution(BASE_PATH, getResultFileName(), selectedVariants, RESULT_FILE_NAME);
			LaneSubstitution.substitution(BASE_PATH, getResultFileName(), selectedVariants, RESULT_FILE_NAME);
			ActivitySupression.activitySupression(BASE_PATH, getResultFileName(), selectedVariants, RESULT_FILE_NAME);
			
			insertarSubprocess(BASE_PATH, getResultFileName(), "Task_1", "Task_1.bpmn", RESULT_FILE_NAME);

			System.out.println("Done");
			 */

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}

	}
	
	public static void insertarSubprocess(String basePath, String baseProcessFileName, String subProcessID, String subProcessFilePath, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		
		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());
		
		Path filepathBase2 = Paths.get(basePath + File.separatorChar + subProcessFilePath);
		DocumentBuilderFactory docFactory2 = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder2 = docFactory2.newDocumentBuilder();
		Document doc2 = docBuilder2.parse(filepathBase2.toString());
		
		Node subProcessNode = ActivitySupression.getTAGNodeByID(doc, "bpmn2:subProcess", subProcessID);
		String incomingFlowID = ActivitySupression.getNodeFlowID(subProcessNode, "bpmn2:incoming");
		Node incomingNode = ActivitySupression.getTAGNodeByID(doc, "bpmn2:sequenceFlow", incomingFlowID);
		String outgoingFlowID = ActivitySupression.getNodeFlowID(subProcessNode, "bpmn2:outgoing");
		Node outgoingNode = ActivitySupression.getTAGNodeByID(doc, "bpmn2:sequenceFlow", outgoingFlowID);
		
		List<Node> nodos = getSubTree(doc2);

		setFlowRef(doc, incomingNode, getTAGID(nodos.get(0)), "targetRef");
		setFlowNode(nodos.get(0), getTAGID(incomingNode), "bpmn2:incoming");
		Node ultNodoSubprocess = nodos.get(nodos.size() - 1);
		setFlowNode(ultNodoSubprocess, outgoingFlowID, "bpmn2:outgoing");
		setFlowRef(doc, outgoingNode, getTAGID(ultNodoSubprocess), "sourceRef");

		doc = insertSubTree(doc, incomingNode, nodos);
		deleteNode(subProcessNode);
		
		System.out.println("-------------- Ya copie todo el subProcess !!!");
		
		ActivitySupression.saveResult(doc, basePath, resultFileName);
	}

	private static void setFlowNode(Node source, String flowID, String tag) {
		Node sourceChild = ((Element) source).getElementsByTagName(tag).item(0);
		sourceChild.setTextContent(flowID);
		System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + flowID + " *-----*");
	}
	
	/*
	public static Node cambiarID(Document doc, Node nodo) {
		Obtener Nodo ID y agregarle "_0" al ID y devolver el nodo modificado
	}
	*/
	
	public static void setFlowRef(Document doc, Node source, String refID, String tag) {
		((Element) source).setAttribute(tag, refID);
		System.out.println("*-----* SETEAMOS " + getTAGID(source) + ":" + tag + " = " + refID + " *-----*");
	}
	
	public static List<Node> getSubTree(Document doc) {
		Node nodo = ActivitySupression.getTAGNodeByID(doc, "bpmn2:startEvent", START_EVENT_SUBPROCESS_ID);
		System.out.println("--- ARMADO DEL ARBOL A EXPORTAR ---");
		List<Node> result = new ArrayList<Node>();
		Node aux = getNextNode(doc, nodo);
		while (!getNodeTAG(doc, getTAGID(aux)).equals("bpmn2:endEvent")) {
			result.add(aux);
			aux = getNextNode(doc, aux);
		}
		System.out.println("\n-- Tamaño subTree: " + result.size() + " --");
		System.out.println("-- Debemos el primer y último SequenceFlow --");
		System.out.println("--- REMOVIENDO ---");
		result = removerPrimerYUltimo(result);
		System.out.println("-- Tamaño subTree: " + result.size() + " --");
		System.out.println("--- FIN ARMADO DEL ARBOL A EXPORTAR ---\n");
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
	
	private static String getResultFileName() {
		if (primero) {
			primero = false;
			return BASE_PROCESS_FILE_NAME;
		}
		return RESULT_FILE_NAME;
	}
	
	public static void replace(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName) throws Exception {

		try {
			BASE_PROCESS_FILE_NAME = baseProcessFileName;
			RESULT_FILE_NAME = resultFileName;
			ActivitySubstitution.activitySubstitution(basePath, getResultFileName(), selectedVariants, resultFileName);
			LaneSubstitution.substitution(basePath, getResultFileName(), selectedVariants, resultFileName);
			ActivitySupression.activitySupression(basePath, getResultFileName(), selectedVariants, resultFileName);
			
			insertarSubprocess(basePath, getResultFileName(), SUBPROCESS_ID, "Task_1.bpmn", resultFileName);

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
