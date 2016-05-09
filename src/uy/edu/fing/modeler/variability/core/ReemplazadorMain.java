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
			selectedVariants.put("Task_1", "S1.bpmn");
			selectedVariants.put("Task_2", DELETE);
			
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
		
		Node subProcessStartEventNode = ActivitySupression.getTAGNodeByID(doc2, "bpmn2:startEvent", "StartEvent_1");
		Node actualActivityProcess = incomingNode;
		Node actualActivitySubProcess = subProcessStartEventNode;
		while (!getNodeTAG(doc2, ((Element) getNextNode(doc2, actualActivitySubProcess)).getAttribute("id")).equals("bpmn2:endEvent")) {
			Node nextSubProcessNode = getNextNode(doc2, subProcessStartEventNode);
			// ------------------------------ INCOMPLETO !!!!!!!!
			/*
			 * HAY QUE IR COPIANDO DE doc2 A doc LOS ELEMENTOS DEL subProcess AL process
			 * ACTUALMENTE NO ESTA BIEN ESTE while
			 */
			
			((Element) actualActivityProcess).setAttribute("targetRef", ((Element) nextSubProcessNode).getAttribute("id"));
			actualActivityProcess = nextSubProcessNode;
			actualActivitySubProcess = nextSubProcessNode;
		}
		System.out.println("-------------- Ya copie todo el subProcess !!!");
		
		
		
		
		ActivitySupression.saveResult(doc, basePath, resultFileName);
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
			}
		}
		System.out.println("---Node Name: " + nodo.getNodeName());
		return nodo.getNodeName();
	}
	
	public static Node getNextNode(Document doc2, Node first) {
		Node nextNode = null;
		
		String outgoingFlowName = ActivitySupression.getNodeFlowID(first, "bpmn2:outgoing");
		Node outgoingFlowNode = getNodeByID(doc2, outgoingFlowName);
		
		String nextNodeName = ((Element) outgoingFlowNode).getAttribute("targetRef");
		nextNode = getNodeByID(doc2, nextNodeName);
		System.out.println("---nextNodeNameNode:" + ((Element) nextNode).getAttribute("id"));
		String nodeTag = getNodeTAG(doc2, ((Element) nextNode).getAttribute("id"));
		System.out.println("---nodeTAG: " + nodeTag);
		
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
			
			//insertarSubprocess(BASE_PATH, getResultFileName(), "Task_1", "Task_1.bpmn", RESULT_FILE_NAME);

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
