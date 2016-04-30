package uy.edu.fing.modeler.variability.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class VariabilityProcess {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {

		Document base = loadXML("test/base1.bpmn");
		Document variant = loadXML("test/base1_1.bpmn");

		Element elementById = base.getElementById("Task_1");
		System.out.println(elementById);
		// NodeList childNodes = elementById.getChildNodes();

		Node elementsByTagName = variant.getElementsByTagName("ext:varPointID").item(0);
		String namedItem = elementsByTagName.getAttributes().getNamedItem("id").getTextContent();
		System.out.println(namedItem);
		elementById.removeAttribute("ext:variability");
		saveXML(base);

	}

	private static void saveXML(Document base) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(base);
		StreamResult streamResult = new StreamResult(new File("test/salida.txt"));
		transformer.transform(source, streamResult);
	}

	private static Document loadXML(String url) throws IOException, ParserConfigurationException, SAXException {
		Path path = Paths.get(url);
		InputStream data = Files.newInputStream(path);
		System.out.println(data.toString());

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document base = dBuilder.parse(data);
		base.getDocumentElement().normalize();
		return base;
	}

}
