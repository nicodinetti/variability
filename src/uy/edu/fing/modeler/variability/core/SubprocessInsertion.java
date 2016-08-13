package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.utils.Utils;

public class SubprocessInsertion {

	public static void subprocessInsertion(String basePath, String baseProcessFileName, String subProcessID, String subProcessFilePath, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Document doc = Utils.getDocument(basePath, baseProcessFileName);
		Document doc2 = Utils.getDocument(basePath + File.separatorChar + "varPoint(" + subProcessID + ")", subProcessFilePath);

		Node subProcessNode = Utils.getNodeByID(doc, subProcessID);
		/*
		 * String incomingFlowID = Utils.getNodeFlowID(subProcessNode,
		 * "bpmn2:incoming"); Node incomingNode = Utils.getTAGNodeByID(doc,
		 * "bpmn2:sequenceFlow", incomingFlowID); String outgoingFlowID =
		 * Utils.getNodeFlowID(subProcessNode, "bpmn2:outgoing"); Node
		 * outgoingNode = Utils.getTAGNodeByID(doc, "bpmn2:sequenceFlow",
		 * outgoingFlowID);
		 */
		List<Node> nodos = Utils.getSubTree(doc2);
		// nodos = Utils.removeFirstAndLastNode(nodos);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("-- Tamaño subTree: " + nodos.size() + " --");
			System.out.println("--- FIN ARMADO DEL ARBOL A EXPORTAR ---\n");
		}
		/*
		 * Utils.setFlowRef(incomingNode, Utils.getTAGID(nodos.get(0)),
		 * "targetRef"); Utils.setFlowNode(nodos.get(0),
		 * Utils.getTAGID(incomingNode), "bpmn2:incoming"); Node
		 * ultNodoSubprocess = nodos.get(nodos.size() - 1);
		 * Utils.setFlowNode(ultNodoSubprocess, outgoingFlowID,
		 * "bpmn2:outgoing"); Utils.setFlowRef(outgoingNode,
		 * Utils.getTAGID(ultNodoSubprocess), "sourceRef");
		 * 
		 * Node incomingFlowNodeEdge = Utils.getNodeByTag(doc,
		 * "bpmndi:BPMNEdge", incomingFlowID); Utils.changeBPMNEdgeTarget(doc,
		 * Utils.getTAGID(incomingFlowNodeEdge), Utils.getTAGID(nodos.get(0)),
		 * "targetElement"); Node outgoingFlowNodeEdge = Utils.getNodeByTag(doc,
		 * "bpmndi:BPMNEdge", outgoingFlowID); Utils.changeBPMNEdgeTarget(doc,
		 * Utils.getTAGID(outgoingFlowNodeEdge),
		 * Utils.getTAGID(ultNodoSubprocess), "sourceElement"); Node
		 * vPIDNodeShape = Utils.getNodeByTag(doc, "bpmndi:BPMNShape",
		 * Utils.getTAGID(subProcessNode)); Utils.figureSupression(doc,
		 * Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");
		 * 
		 * doc = Utils.insertSubTree(doc, incomingNode, nodos);
		 * Utils.deleteNode(subProcessNode);
		 */
		doc = Utils.insertSubTree(doc, subProcessNode, nodos);

		System.out.println("-------------- Ya copie todo el subProcess !!!");

		Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

		Path target = Paths.get(basePath + File.separatorChar + "varPoint(" + subProcessID + ")" + File.separatorChar + subProcessFilePath);
		System.out.println("-------------- VAMOS A BORRAR EL ARCHIVO " + target + " !!!");
		// Files.delete(target);
	}

}
