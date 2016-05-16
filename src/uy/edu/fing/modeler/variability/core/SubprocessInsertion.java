package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SubprocessInsertion {
	
	public static void subprocessInsertion(String basePath, String baseProcessFileName, String subProcessID, String subProcessFilePath, String resultFileName)
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
		
		List<Node> nodos = Utils.getSubTree(doc2);
		nodos = Utils.removerPrimerYUltimo(nodos);
		System.out.println("-- Tamaño subTree: " + nodos.size() + " --");
		System.out.println("--- FIN ARMADO DEL ARBOL A EXPORTAR ---\n");

		Utils.setFlowRef(doc, incomingNode, Utils.getTAGID(nodos.get(0)), "targetRef");
		Utils.setFlowNode(nodos.get(0), Utils.getTAGID(incomingNode), "bpmn2:incoming");
		Node ultNodoSubprocess = nodos.get(nodos.size() - 1);
		Utils.setFlowNode(ultNodoSubprocess, outgoingFlowID, "bpmn2:outgoing");
		Utils.setFlowRef(doc, outgoingNode, Utils.getTAGID(ultNodoSubprocess), "sourceRef");
		
		Node incomingFlowNodeEdge = ActivitySupression.getTAGNodeByBPMNElement(doc, "bpmndi:BPMNEdge", incomingFlowID);
		ActivitySupression.changeBPMNEdgeTarget(doc, Utils.getTAGID(incomingFlowNodeEdge), Utils.getTAGID(nodos.get(0)), "targetElement");
		Node outgoingFlowNodeEdge = ActivitySupression.getTAGNodeByBPMNElement(doc, "bpmndi:BPMNEdge", outgoingFlowID);
		ActivitySupression.changeBPMNEdgeTarget(doc, Utils.getTAGID(outgoingFlowNodeEdge), Utils.getTAGID(ultNodoSubprocess), "sourceElement");
		Node vPIDNodeShape = ActivitySupression.getTAGNodeByBPMNElement(doc, "bpmndi:BPMNShape", Utils.getTAGID(subProcessNode));
		ActivitySupression.figureSupression(doc, Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");


		doc = Utils.insertSubTree(doc, incomingNode, nodos);
		Utils.deleteNode(subProcessNode);
		
		System.out.println("-------------- Ya copie todo el subProcess !!!");
		
		ActivitySupression.saveResult(doc, basePath, resultFileName);
	}


}