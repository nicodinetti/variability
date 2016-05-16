package uy.edu.fing.modeler.variability.core;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LaneSubstitution {

	public static void laneSubstitution(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Map<String, String> filterSelecteds = filterSelecteds(selectedVariants);

		if (filterSelecteds.isEmpty()) {
			return;
		}

		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());

		String activity = filterSelecteds.keySet().iterator().next();
		String lane = filterSelecteds.get(activity);

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
				searchActivity.getParentNode().removeChild(searchActivity);
				break;
			}

		}
		// Buscar el lane y agregarle la actividad
		for (int it = 0; it < lanes.getLength(); it++) {
			Node nodo = lanes.item(it);
			String laneID = nodo.getAttributes().getNamedItem("id").getTextContent();
			System.out.println(laneID);
			if (laneID.equals(lane)) {
				nodo.appendChild(searchActivity);
				break;
			}

		}

		saveResult(doc, basePath, resultFileName);
	}

	private static Map<String, String> filterSelecteds(Map<String, String> selectedVariants) {

		Map<String, String> result = selectedVariants.keySet().stream().filter(x -> !selectedVariants.get(x).equals(ReemplazadorMain.DELETE) && !selectedVariants.get(x).endsWith("bpmn"))
				.collect(Collectors.toMap(x -> x, x -> selectedVariants.get(x)));
		return result;
	}

	private static void saveResult(Document doc, String basePath, String fileName) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		Path filepathResult = Paths.get(basePath + File.separatorChar + fileName);
		StreamResult result = new StreamResult(new File(filepathResult.toString()));
		transformer.transform(source, result);
	}

}
