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

		List<Node> nodos = Utils.getSubTree(doc2);
		// nodos = Utils.removeFirstAndLastNode(nodos);
		if (ReemplazadorMain.IMPRIMIR_LOG_SUBPROCESS) {
			System.out.println("-- Tamaño subTree: " + nodos.size() + " --");
			System.out.println("--- FIN ARMADO DEL ARBOL A EXPORTAR ---\n");
		}

		doc = Utils.insertSubTree(doc, subProcessNode, nodos);

		doc = Utils.insertExtras(doc, doc2);

		System.out.println("-------------- Ya copie todo el subProcess !!!");

		Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

		Path target = Paths.get(basePath + File.separatorChar + "varPoint(" + subProcessID + ")" + File.separatorChar + subProcessFilePath);
		System.out.println("-------------- VAMOS A BORRAR EL ARCHIVO " + target + " !!!");
		// Files.delete(target);
	}

}
