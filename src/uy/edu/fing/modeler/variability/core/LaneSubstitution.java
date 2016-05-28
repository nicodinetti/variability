package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

public class LaneSubstitution {

	public static void laneSubstitution(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Map<String, String> filterSelecteds = filterSelecteds(selectedVariants);

		if (filterSelecteds.isEmpty()) {
			LogUtils.log(baseProcessFileName, "No hay Lanes para sustituir");
			return;
		}

		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());

		String activity = filterSelecteds.keySet().iterator().next();
		LogUtils.log(baseProcessFileName, "Actividad: " + activity);

		String lane = filterSelecteds.get(activity);
		LogUtils.log(baseProcessFileName, "Lane: " + lane);

		NodeList lanes = doc.getElementsByTagName("bpmn2:lane");

		// Buscar y eliminar la actividad
		Node searchActivity = null;
		for (int it = 0; it < lanes.getLength(); it++) {
			Node nodo = lanes.item(it);
			NodeList flowNodeRefs = nodo.getChildNodes();
			for (int j = 0; j < flowNodeRefs.getLength(); j++) {
				Node flowNodeRef = flowNodeRefs.item(j);
				String attr = flowNodeRef.getTextContent();
				System.out.println(j + " - " + attr);
				if (attr.equals(activity)) {
					searchActivity = flowNodeRef;
					break;
				}
			}

			if (searchActivity != null) {
				//searchActivity.getParentNode().removeChild(searchActivity);
				Utils.deleteNode(searchActivity);
				LogUtils.log(baseProcessFileName, "Actividad encontrada y eliminada");
				break;
			}

		}
		if (searchActivity == null) {
			LogUtils.log(baseProcessFileName, "ERROR: No existe esa Actividad !!!");
		}
		// Buscar el lane y agregarle la actividad
		boolean encontre_lane = false;
		for (int it = 0; it < lanes.getLength(); it++) {
			Node nodo = lanes.item(it);
			String laneID = nodo.getAttributes().getNamedItem("id").getTextContent();
			System.out.println(laneID);
			if (laneID.equals(lane)) {
				encontre_lane = true;
				nodo.appendChild(searchActivity);
				LogUtils.log(baseProcessFileName, "Agregada la actividad al Lane seleccionado");
				break;
			}
		}
		if (!encontre_lane){
			// Creo el nuevo Lane
			Element newLane = doc.createElement("bpmn2:lane");
			newLane.setAttribute("id", lane);
			newLane.setAttribute("name", lane);
			Node parentNode = lanes.item(0).getParentNode();
			doc.importNode(newLane, true);
			parentNode.appendChild(newLane);
			
			// Inserta la Activity en el nuevo Lane
			newLane.appendChild(searchActivity);			
			LogUtils.log(baseProcessFileName, "No existía el Lane. Creado el Lane con la actividad seleccionada dentro.");
		}

		Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);
	}

	private static Map<String, String> filterSelecteds(Map<String, String> selectedVariants) {
		Map<String, String> result = selectedVariants.keySet().stream().filter(x -> !selectedVariants.get(x).equals(ReemplazadorMain.DELETE) && !selectedVariants.get(x).endsWith("bpmn"))
				.collect(Collectors.toMap(x -> x, x -> selectedVariants.get(x)));
		return result;
	}

}
