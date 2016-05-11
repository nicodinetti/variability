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
			selectedVariants.put("Task_1", "S2.bpmn");
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
		String incomingFlow = ActivitySupression.getNodeFlowID(subProcessNode, "bpmn2:incoming");
		Node incomingNode = ActivitySupression.getTAGNodeByID(doc, "bpmn2:sequenceFlow", incomingFlow);
		
		Node actualActivitySubProcess = ActivitySupression.getTAGNodeByID(doc2, "bpmn2:startEvent", "StartEvent_10");
		Node actualActivityProcess = incomingNode;
		
		List<Node> nodos = getSubTree(doc2, actualActivitySubProcess);
		doc = insertSubTree(doc, actualActivityProcess, nodos);
		
		
		/*
		int i = 0;
		while (!getNodeTAG(doc2, ((Element) getNextNode(doc2, actualActivitySubProcess)).getAttribute("id")).equals("bpmn2:endEvent")) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			Node nextSubProcessNode = getNextNode(doc2, actualActivitySubProcess);
			System.out.println("--- actualActivityProcess ID: " + ((Element)actualActivityProcess).getAttribute("id"));
			System.out.println("--- nextSubProcessNode ID: " + ((Element)nextSubProcessNode).getAttribute("id"));

			doc.importNode(nextSubProcessNode, true);
			doc.adoptNode(nextSubProcessNode);
			actualActivityProcess.getParentNode().appendChild(nextSubProcessNode);
			System.out.println("----------- Agregamos el elemento " + ((Element) nextSubProcessNode).getAttribute("id") + " al Process");
			
			if (isSequenceFlow(getNodeTAG(doc, ((Element)actualActivityProcess).getAttribute("id")))) {
				System.out.println("//-------------------- ES SF");
				((Element) actualActivityProcess).setAttribute("targetRef", ((Element) nextSubProcessNode).getAttribute("id"));
				System.out.println("*------* SETEAMOS EL targetRef DEL " + ((Element)actualActivityProcess).getAttribute("id") 
						+ " CON EL " + ((Element)nextSubProcessNode).getAttribute("id"));
			} else {
				System.out.println("//-------------------- NO ES SF");
				// HAY QUE MODIFICAR EL outgoingFlow
				String incomingFlowAux = ActivitySupression.getNodeFlowID(actualActivityProcess, "bpmn2:incoming");
				Node aux = ActivitySupression.getTAGNodeByID(doc, "bpmn2:task", ((Element)actualActivityProcess).getAttribute("id"));
				Node primero = aux.getFirstChild();
				primero.setNodeValue(incomingFlowAux);
				
				System.out.println("*------* SETEAMOS EL outgoingFlow DEL " + ((Element)actualActivityProcess).getAttribute("id") 
						+ " CON " + ((Element)nextSubProcessNode).getAttribute("id"));
			};
			actualActivityProcess = nextSubProcessNode;
			actualActivitySubProcess = nextSubProcessNode;
			ActivitySupression.saveResult(doc, basePath, resultFileName);
			if (i == 1)
				break;
		}
		*/
	
		System.out.println("-------------- Ya copie todo el subProcess !!!");
		
		
		ActivitySupression.saveResult(doc, basePath, resultFileName);
	}
	
	/*
	public static Node cambiarID(Document doc, Node nodo) {
		Obtener Nodo ID y agregarle "_0" al ID y devolver el nodo modificado
	}
	*/
	
	public static List<Node> getSubTree(Document doc, Node nodo) {
		List<Node> result = new ArrayList<Node>();
		Node aux = getNextNode(doc, nodo);
		while (!getNodeTAG(doc, getTAGID(aux)).equals("bpmn2:endEvent")) {
			System.out.println("---------");
			result.add(aux);
			aux = getNextNode(doc, aux);
		}
		return result;
	}
	
	public static Document insertSubTree(Document doc, Node first, List<Node> nodos) {
		System.out.println("*** Tamaño subTree: " + nodos.size());
		Node parentNode = first.getParentNode();
		for (Node node : nodos) {
			doc.importNode(node, true);
			doc.adoptNode(node);
			parentNode.appendChild(node);
			System.out.println("------------------------ INSERTAMOS: " + getTAGID(node));
		}
		return doc;
	}
	
	public static Node getNodeByID(Document doc, String nodoID) {
		return ActivitySupression.getTAGNodeByID(doc, getNodeTAG(doc, nodoID), nodoID);
	}
	
	public static String getNodeTAG(Document doc, String nodoID) {
		Node aux = null;
		Node nodo = null;
		List<String> tags = Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask", "bpmn2:receiveTask", "bpmn2:startEvent", "bpmn2:endEvent", "bpmn2:sequenceFlow", "bpmn2:subProcess", "bpmn2:process", "bpmndi:BPMNDiagram", "bpmndi:BPMNShape", "bpmndi:BPMNEdge");
//		System.out.println("+ getNodeTAG - nodoID: " + nodoID);
		for (String tag : tags) {
			aux = ActivitySupression.getTAGNodeByID(doc, tag, nodoID);
			if (aux != null) {
//				System.out.println("+ getNodeTAG - TAG: " + tag);
				nodo = aux;
				break;
			}
		}
		//System.out.println("---Node Name: " + nodo.getNodeName());
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
		
		System.out.println("Node ID: " + getTAGID(first));
		System.out.println("Node TAG: " + getNodeTAG(doc2, getTAGID(first)));
		
		String outgoingName = "";
		
		if (isTask(getNodeTAG(doc2, getTAGID(first)))) {
			System.out.println("*** Es TASK");
			outgoingName = ActivitySupression.getNodeFlowID(first, "bpmn2:outgoing");
			
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			
			nextNode = outgoingFlowNode;			
			/*
			String nextNodeName = ((Element) outgoingFlowNode).getAttribute("targetRef");
			System.out.println("***** nextNodeName: " + nextNodeName);
			nextNode = getNodeByID(doc2, nextNodeName);
			
			String nodeTag = getNodeTAG(doc2, getTAGID(nextNode));
			*/
			
		} else if (isSequenceFlow(getNodeTAG(doc2, getTAGID(first)))) {
			
			// REVISAR -- NO SE ESTAN INSERTANDO LOS SF
			
			System.out.println("*** Es SF");
			outgoingName = ((Element) first).getAttribute("targetRef");
			
			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			
			nextNode = outgoingFlowNode;
			/*			
			String outgoingFlow = ActivitySupression.getNodeFlowID(outgoingFlowNode, "bpmn2:outgoing");
			nextNode = ActivitySupression.getTAGNodeByID(doc2, "bpmn2:sequenceFlow", outgoingFlow);
			*/	
			
		} else {
			System.out.println("*** NO es TASK NI SF");
			outgoingName = ActivitySupression.getNodeFlowID(first, "bpmn2:outgoing");

			Node outgoingFlowNode = getNodeByID(doc2, outgoingName);
			
			nextNode = outgoingFlowNode;
			/*
			String nextNodeName = ((Element) outgoingFlowNode).getAttribute("targetRef");
			System.out.println("***** nextNodeName: " + nextNodeName);
			nextNode = getNodeByID(doc2, nextNodeName);
			
			String nodeTag = getNodeTAG(doc2, getTAGID(nextNode));
			*/
		};
		System.out.println("---outgoingFlowName: " + outgoingName);
		
		return nextNode;
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
			
			insertarSubprocess(basePath, getResultFileName(), "Task_1", "Task_1.bpmn", resultFileName);

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
