package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

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

		Document doc = Utils.getDocument(basePath, baseProcessFileName);

		if (filterSelecteds.isEmpty()) {
			LogUtils.log(baseProcessFileName, "No hay Lanes para sustituir");
			Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);
			return;
		}

		// Map<String, String> selectedVariantsForSubstitution = new
		// HashMap<>();

		Iterator<String> keyIiterator = filterSelecteds.keySet().iterator();
		while (keyIiterator.hasNext()) {
			String key = keyIiterator.next();
			String activity = key.substring(key.lastIndexOf(File.separatorChar) + 1, key.length());

			LogUtils.log(baseProcessFileName, "Actividad: " + key);

			String variante = filterSelecteds.get(key);
			Path path = Paths.get(basePath + File.separatorChar + "varPoint(" + activity + ")" + File.separatorChar + variante);

			if (Files.exists(path)) {

				String newBasePath = path.getParent().toString();
				String newFileName = path.getFileName().toString();
				Document doc2 = Utils.getDocument(newBasePath, newFileName);

				Node nodoLane = getVariabilityLane(doc2);
				if (nodoLane == null) {
					// No hay Lane
					break;
				}

				String lane = Utils.getTAGID(nodoLane);
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
						Utils.deleteNode(searchActivity);
						LogUtils.log(baseProcessFileName, "Actividad encontrada y eliminada");
						break;
					}

				}

				if (searchActivity == null) {
					LogUtils.log(baseProcessFileName, "ERROR: No existe esa Actividad !!!");
					return;
				}

				// Buscar el lane y agregarle la actividad
				Element nodoLaneBuscado = null;
				int lane_index = getLaneIndex(lane, lanes);
				if (lane_index != -1) {
					nodoLaneBuscado = (Element) lanes.item(lane_index);
				} else {
					nodoLaneBuscado = createLane(doc, nodoLane, lanes.item(0).getParentNode());
					LogUtils.log(baseProcessFileName, "No existia el Lane. Creado el Lane !");
				}
				nodoLaneBuscado.appendChild(searchActivity);

				LogUtils.log(baseProcessFileName, "Agregada la actividad al Lane seleccionado");

				Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

				// selectedVariantsForSubstitution.put(key, variante);
				System.out.println("--- Activity: " + key);
				System.out.println("--- Variante: " + variante);
			}

			// LogUtils.logNext(baseProcessFileName, "LANE SUBSTITUTION: Ini
			// laneSubstitution");
			// ActivitySubstitution.activitySubstitution(basePath,
			// resultFileName, selectedVariantsForSubstitution, resultFileName);
			// LogUtils.logNext(baseProcessFileName, "LANE SUBSTITUTION: FIN
			// laneSubstitution");
		}
	}

	private static Node getVariabilityLane(Document doc) {
		NodeList elementsByTagName = doc.getElementsByTagName("bpmn2:lane");
		return elementsByTagName != null ? elementsByTagName.item(0) : null;
	}

	private static Element createLane(Document doc, Node nodoLane, Node parentNode) {
		Element newLane = doc.createElement("bpmn2:lane");
		newLane.setAttribute("id", Utils.getTAGID(nodoLane));
		newLane.setAttribute("name", Utils.getTAGName(nodoLane));
		doc.importNode(newLane, true);
		parentNode.appendChild(newLane);
		return newLane;
	}

	private static int getLaneIndex(String lane, NodeList lanes) {
		for (int it = 0; it < lanes.getLength(); it++) {
			Node nodoLane = lanes.item(it);
			String laneID = Utils.getTAGID(nodoLane);
			if (laneID.equals(lane)) {
				return it;
			}
		}
		return -1;
	}

	private static Map<String, String> filterSelecteds(Map<String, String> selectedVariants) {
		Map<String, String> result = selectedVariants.keySet().stream().filter(x -> !selectedVariants.get(x).equals(ReemplazadorMain.DELETE))
				.collect(Collectors.toMap(x -> x, x -> selectedVariants.get(x)));
		return result;
	}

}
