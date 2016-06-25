package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.log.LogUtils;
import uy.edu.fing.modeler.variability.utils.Utils;

public class ActivitySupression {

    public static void activitySupression(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
        throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

        Document doc = Utils.getDocument(basePath, baseProcessFileName);

        List<Node> vPNodes = Utils.getVPListByType(doc, "bpmn2:task", "VPTask");
        vPNodes.addAll(Utils.getVPListByType(doc, "bpmn2:subProcess", "VPSubProcess"));

        LogUtils.log(baseProcessFileName, "VPs a suprimir: " + vPNodes);

        for (Node vPNode : vPNodes) {

            String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();

            String key = basePath + File.separatorChar + baseProcessFileName + File.separatorChar + vPID;
            String selectedVariant = selectedVariants.get(key);

            if (ReemplazadorMain.DELETE.equals(selectedVariant)) {

                LogUtils.logNext(baseProcessFileName, "Suprimir: " + vPID);

                String incomingFlow = Utils.getNodeFlowID(vPNode, "bpmn2:incoming");
                String outgoingFlow = Utils.getNodeFlowID(vPNode, "bpmn2:outgoing");

                Node incomingNode = Utils.getTAGNodeByID(doc, "bpmn2:sequenceFlow", incomingFlow);
                Node outgoingNode = Utils.getTAGNodeByID(doc, "bpmn2:sequenceFlow", outgoingFlow);

                String targetRefFinal = ((Element) outgoingNode).getAttribute("targetRef");
                ((Element) incomingNode).setAttribute("targetRef", targetRefFinal);

                Node targetRefFinalNode = Utils.getTargetRefFinalNode(doc, targetRefFinal);

                Node targetRefFinalNodeIncoming = ((Element) targetRefFinalNode).getElementsByTagName("bpmn2:incoming").item(0);
                targetRefFinalNodeIncoming.setTextContent(Utils.getTAGID(incomingNode));

                // Eliminar la tarea si estaba dentro de un lane
                NodeList lanes = doc.getElementsByTagName("bpmn2:lane");

                Node searchActivity = null;
                for (int it = 0; it < lanes.getLength(); it++) {
                    Node nodo = lanes.item(it);
                    NodeList flowNodeRefs = nodo.getChildNodes();
                    for (int j = 0; j < flowNodeRefs.getLength(); j++) {
                        Node flowNodeRef = flowNodeRefs.item(j);
                        String attr = flowNodeRef.getTextContent();
                        if (attr.equals(vPID)) {
                            searchActivity = flowNodeRef;
                            break;
                        }
                    }

                    if (searchActivity != null) {
                        Utils.deleteNode(searchActivity);
                        LogUtils.log(baseProcessFileName, "Actividad encontrada y eliminada");
                        break;
                    }
                }

                Node outgoingFlowNodeEdge = Utils.getNodeByTag(doc, "bpmndi:BPMNEdge", outgoingFlow);
                Node incomingFlowNodeEdge = Utils.getNodeByTag(doc, "bpmndi:BPMNEdge", incomingFlow);
                Node vPIDNodeShape = Utils.getNodeByTag(doc, "bpmndi:BPMNShape", vPID);
                Node targetRefFinalNodeShape = Utils.getNodeByTag(doc, "bpmndi:BPMNShape", targetRefFinal);

                // Eliminacion del FIGURE a borrar
                Utils.figureSupression(doc, Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");
                Utils.figureSupression(doc, Utils.getTAGID(vPIDNodeShape), "bpmndi:BPMNShape");
                Utils.figureSupression(doc, Utils.getTAGID(outgoingFlowNodeEdge), "bpmndi:BPMNEdge");
                Utils.changeBPMNEdgeTarget(doc, Utils.getTAGID(incomingFlowNodeEdge), Utils.getTAGID(targetRefFinalNodeShape), "targetElement");

                // Eliminacion del TAG a borrar
                Node nodoPadre = vPNode.getParentNode();
                nodoPadre.removeChild(vPNode);

                nodoPadre = outgoingNode.getParentNode();
                nodoPadre.removeChild(outgoingNode);

            } else {
                LogUtils.logNext(baseProcessFileName, "NO suprimir: " + vPID);
            }

            LogUtils.back();
        }

        Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);
    }

}
