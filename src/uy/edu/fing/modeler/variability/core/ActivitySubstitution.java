package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.log.LogUtils;
import uy.edu.fing.modeler.variability.utils.Utils;

public class ActivitySubstitution {

	private static final boolean PRINT_LOGS = ReemplazadorMain.PRINT_LOGS;

	public static Map<String, String> activitySubstitution(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Map<String, String> res = new HashMap<>();

		Document doc = Utils.getDocument(basePath, baseProcessFileName);
		DocumentBuilder docBuilder = Utils.getDocumentBuilder();

		List<Node> vPNodes = Utils.getVPListByType(doc, "bpmn2:task", "VPTask");
		vPNodes.addAll(Utils.getVPListByType(doc, "bpmn2:subProcess", "VPSubProcess"));

		if (PRINT_LOGS) {
			LogUtils.log(baseProcessFileName, "VPs a reemplazar: " + vPNodes);
		}

		for (Node vPNode : vPNodes) {

			LogUtils.logNext(baseProcessFileName, "A reemplazar: " + Utils.getTAGID(vPNode));

			Node vNode = getVariant(docBuilder, basePath, baseProcessFileName, vPNode, Arrays.asList("bpmn2:startEvent"), selectedVariants);
			// Es un subProcess
			if (vNode != null) {
				vNode = getVariant(docBuilder, basePath, baseProcessFileName, vPNode, Arrays.asList("bpmn2:process"), selectedVariants);
				if (PRINT_LOGS) {
					LogUtils.log(baseProcessFileName, "Se reemplaza por un subproceso: " + vNode);
				}
			}

			// Es una Task
			if (vNode == null) {
				vNode = getVariant(docBuilder, basePath, baseProcessFileName, vPNode, Utils.tasks, selectedVariants);
				if (PRINT_LOGS) {
					LogUtils.log(baseProcessFileName, "Se reemplaza por una tarea: " + vNode);
				}
			}

			if (vNode != null) {
				String vTagName = ((Element) vNode).getTagName();
				if (vTagName.equals("bpmn2:process")) {

					LogUtils.log(baseProcessFileName, "No se reemplaza por Task. Copiar el archivo correspondiente al subproceso " + vNode.getNodeName());

					String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();
					String key = basePath + File.separatorChar + baseProcessFileName + File.separatorChar + vPID;
					String selectedfileName = selectedVariants.get(key);
					res.put(vPID, selectedfileName);

				}

				// Copiar el nombre
				copyName(vPNode, vNode);
				copyAttributes(vPNode, vNode);
				changeTagName(doc, vPNode, vNode);
			}

			LogUtils.back();

		}

		Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

		return res;
	}

	private static void changeTagName(Document doc, Node vPNode, Node vNode) {
		String tagName = ((Element) vNode).getTagName();
		String vTagName = tagName.equals("bpmn2:process") ? "bpmn2:subProcess" : tagName;
		doc.renameNode(vPNode, "http://www.omg.org/spec/BPMN/20100524/MODEL", vTagName);

		if (PRINT_LOGS) {
			LogUtils.log(vPNode.getNodeName(), "Copiado el tagName: " + vTagName);
		}
	}

	private static void copyAttributes(Node vPNode, Node vNode) {
		NamedNodeMap vPAttr = vPNode.getAttributes();

		// Listar atributos menos ID
		List<String> listAttr = new ArrayList<String>();
		for (int i = 0; i < vPAttr.getLength(); i++) {
			String nodeName = vPAttr.item(i).getNodeName();
			if (!nodeName.equals("id")) {
				listAttr.add(nodeName);
			}
		}

		// Eliminar los atributos listados
		for (String name : listAttr) {
			vPAttr.removeNamedItem(name);
		}

		// Agregar los atributos de la variante, menos el ID y el isExecutable
		NamedNodeMap vAttr = vNode.getAttributes();
		for (int i = 0; i < vAttr.getLength(); i++) {
			String nodeName2 = vAttr.item(i).getNodeName();
			if (!nodeName2.equals("id") && !nodeName2.equals("isExecutable")) {
				((Element) vPNode).setAttribute(nodeName2, vAttr.item(i).getNodeValue());
			}
		}

		if (PRINT_LOGS) {
			LogUtils.log(vPNode.getNodeName(), "Copiados los atributos: " + listAttr);
		}
	}

	private static void copyName(Node vPNode, Node vNode) {
		String vName = vNode.getAttributes().getNamedItem("name").getTextContent();
		vPNode.getAttributes().getNamedItem("name").setTextContent(vName);
		if (PRINT_LOGS) {
			LogUtils.log(vPNode.getNodeName(), "Copiado el nombre: " + vName);
		}
	}

	@SuppressWarnings("resource")
	private static Node getVariant(DocumentBuilder docBuilder, String basePath, String baseFileName, Node vPNode, List<String> types, Map<String, String> selectedVariants)
			throws IOException, Exception, SAXException {
		LogUtils.next();

		String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();
		String key = basePath + File.separatorChar + baseFileName + File.separatorChar + vPID;
		if (PRINT_LOGS) {
			LogUtils.log(vPNode.getNodeName(), "Obtener variante para: " + key);
		}

		if (ReemplazadorMain.DELETE.equals(selectedVariants.get(key))) {
			if (PRINT_LOGS) {
				LogUtils.log(vPNode.getNodeName(), "No es necesario para DELETE");
			}
			LogUtils.back();
			return null;
		}

		Path path = Paths.get(basePath + File.separatorChar + "varPoint(" + vPID + ")");
		Stream<Path> list = Files.list(path);
		Node vNode = null;
		if ((!Files.exists(path) || !Files.isDirectory(path) || list.count() == 0)) {
			if (PRINT_LOGS) {
				LogUtils.log(vPNode.getNodeName(), "No se encontraron variantes");
			}
			throw new Exception("No existen variantes para el VP: " + vPID);
		} else if (!ReemplazadorMain.DELETE.equals(selectedVariants.get(key)) && selectedVariants.get(key) != null) {
			vNode = getVariantImpl(docBuilder, path, types, selectedVariants.get(key));
		}
		if (PRINT_LOGS) {
			LogUtils.log(vPNode.getNodeName(), "Variante seleccionada: " + vNode);
		}
		LogUtils.back();
		return vNode;
	}

	private static Node getVariantImpl(DocumentBuilder docBuilder, Path path, List<String> types, String selectedVariant) throws IOException, SAXException, Exception {
		String variantPathString = Paths.get(path.toString() + File.separatorChar + selectedVariant).toString();
		Document variantDoc = docBuilder.parse(variantPathString);
		List<Node> vNodes = types.stream().map(t -> getVListByType(variantDoc, t)).flatMap(Collection::stream).collect(Collectors.toList());
		if (vNodes.size() == 0) {
			return null;
		} else if (vNodes.size() > 1) {
			throw new Exception("No existe una única variant como variante");
		}
		Node vNode = vNodes.get(0);
		return vNode;
	}

	private static List<Node> getVListByType(Document doc, String type) {
		NodeList nodeList = doc.getElementsByTagName(type);
		List<Node> variants = new ArrayList<Node>();
		Node nodo = null;
		int length = nodeList.getLength();

		for (int it = 0; it < length; it++) {
			nodo = nodeList.item(it);
			variants.add(nodo);
		}
		return variants;
	}
}
