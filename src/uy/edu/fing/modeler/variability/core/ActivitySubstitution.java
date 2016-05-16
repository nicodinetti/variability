package uy.edu.fing.modeler.variability.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ActivitySubstitution {

	public static Map<String, String> activitySubstitution(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		Map<String, String> res = new HashMap<>();

		Path filepathBase = Paths.get(basePath + File.separatorChar + baseProcessFileName);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepathBase.toString());

		List<Node> vPNodes = getVPListByType(doc, "bpmn2:task", "VPTask");
		vPNodes.addAll(getVPListByType(doc, "bpmn2:subProcess", "VPSubProcess"));

		System.out.println("Nodos VP: " + vPNodes);

		for (Node vPNode : vPNodes) {

			Node vNode = getVariant(docBuilder, basePath, vPNode, Arrays.asList("bpmn2:startEvent"), selectedVariants);
			// Es un subProcess
			if (vNode != null) {
				vNode = getVariant(docBuilder, basePath, vPNode, Arrays.asList("bpmn2:process"), selectedVariants);
				System.out.println("Es un subproceso: " + vPNode + " a ser reemplazado por " + vNode);
			}

			// Es una Task
			if (vNode == null) {
				vNode = getVariant(docBuilder, basePath, vPNode,
						Arrays.asList("bpmn2:task", "bpmn2:userTask", "bpmn2:manualTask", "bpmn2:scriptTask", "bpmn2:businessRuleTask", "bpmn2:serviceTask", "bpmn2:sendTask", "bpmn2:receiveTask"),
						selectedVariants);
				System.out.println("Es una tarea: " + vPNode + " a ser reemplazada por " + vNode);
			}

			if (vNode != null) {
				String vTagName = ((Element) vNode).getTagName();
				if (vTagName.equals("bpmn2:process")) {
					String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();
					String selectedfileName = selectedVariants.get(vPID);
					Path source = Paths.get(basePath + File.separatorChar + vPID + File.separatorChar + selectedfileName);
					Path target = Paths.get(basePath + File.separatorChar + vPID + ".bpmn");
					Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

					System.out.println("El subproceso fue copiado al archivo " + target.getFileName() + " y es referenciado desde el proceso base como " + vPID);
					res.put(vPID, target.getFileName().toString());
				}

				// Copiar el nombre
				copyName(vPNode, vNode);
				copyAttributes(vPNode, vNode);
				changeTagName(doc, vPNode, vNode);
			}

		}
		saveResult(doc, basePath, resultFileName);

		return res;
	}

	private static void saveResult(Document doc, String basePath, String fileName) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		Path filepathResult = Paths.get(basePath + File.separatorChar + fileName);
		StreamResult result = new StreamResult(new File(filepathResult.toString()));
		transformer.transform(source, result);
	}

	private static void changeTagName(Document doc, Node vPNode, Node vNode) {
		String tagName = ((Element) vNode).getTagName();
		String vTagName = tagName.equals("bpmn2:process") ? "bpmn2:subProcess" : tagName;

		doc.renameNode(vPNode, "http://www.omg.org/spec/BPMN/20100524/MODEL", vTagName);

		System.out.println("Copiado del tagName: " + vTagName);
	}

	private static void copyAttributes(Node vPNode, Node vNode) {
		NamedNodeMap vPAttr = vPNode.getAttributes();

		// Listar atributos menos ID
		List<String> listAttr = new ArrayList<String>();
		for (int i = 0; i < vPAttr.getLength(); i++) {
			String nodeName = vPAttr.item(i).getNodeName();
			if (!nodeName.equals("id"))
				listAttr.add(nodeName);
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

		System.out.println("Copiados los atributos: " + listAttr);
	}

	private static void copyName(Node vPNode, Node vNode) {
		String vName = vNode.getAttributes().getNamedItem("name").getTextContent();
		vPNode.getAttributes().getNamedItem("name").setTextContent(vName);
		System.out.println("Copiado el nombre: " + vName);
	}

	@SuppressWarnings("resource")
	private static Node getVariant(DocumentBuilder docBuilder, String basePath, Node vPNode, List<String> types, Map<String, String> selectedVariants) throws IOException, Exception, SAXException {

		String vPID = vPNode.getAttributes().getNamedItem("id").getNodeValue();

		if (ReemplazadorMain.DELETE.equals(selectedVariants.get(vPID))) {
			return null;
		}

		Path path = Paths.get(basePath + File.separatorChar + vPID);
		Stream<Path> list = Files.list(path);
		Node vNode = null;
		if ((!Files.exists(path) || !Files.isDirectory(path) || list.count() == 0)) {
			throw new Exception("No existen variantes para el VP: " + vPID);
		} else if (!ReemplazadorMain.DELETE.equals(selectedVariants.get(vPID))) {
			vNode = getVariantImpl(docBuilder, path, types, selectedVariants.get(vPID));
		}
		return vNode;
	}

	@SuppressWarnings("resource")
	private static Node getVariantImpl(DocumentBuilder docBuilder, Path path, List<String> types, String selectedVariant) throws IOException, SAXException, Exception {
		Stream<Path> list = Files.list(path);
		Path variantPath = list.filter(x -> x.getFileName().toString().equals(selectedVariant)).findFirst().get();
		Document variantDoc = docBuilder.parse(variantPath.toString());
		List<Node> vNodes = types.stream().map(t -> getVListByType(variantDoc, t)).flatMap(Collection::stream).collect(Collectors.toList());
		if (vNodes.size() == 0) {
			return null;
		} else if (vNodes.size() > 1) {
			throw new Exception("No existe una única variant como variante");
		}
		Node vNode = vNodes.get(0);
		return vNode;
	}

	private static List<Node> getVPListByType(Document doc, String type, String variability) {
		NodeList nodeList = doc.getElementsByTagName(type);
		List<Node> variationPoints = new ArrayList<Node>();
		for (int it = 0; it < nodeList.getLength(); it++) {
			Node nodo = nodeList.item(it);
			NamedNodeMap attr = nodo.getAttributes();
			Node nodeAttr = attr.getNamedItem("ext:variability");
			if (nodeAttr != null && variability.equals(nodeAttr.getTextContent())) {
				variationPoints.add(nodo);
			}
		}
		return variationPoints;
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
