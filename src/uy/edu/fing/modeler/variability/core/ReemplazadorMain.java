package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class ReemplazadorMain {

	public static final boolean IMPRIMIR_LOG_SUBPROCESS = false;

	public static final String DELETE = "DELETE";

	public static void main(String[] args) throws Exception {
		try {

			Map<String, String> selectedVariants = new HashMap<>();
			selectedVariants.put("Task_1", "S2.bpmn");
			// String basePath =
			// "/home/abrusco/git/variability/test/reemplazador";
			String basePath = "/Users/ndinetti/Desarrollo/sourcecode/variability/test/reemplazador";
			String baseProcessFileName = "process_2.bpmn";
			String resultFileName = "result.bpmn";

			replace(basePath, baseProcessFileName, selectedVariants, resultFileName);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}

	}

	public static void replace(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName) throws Exception {

		try {

			Map<String, String> subprocessResult = ActivitySubstitution.activitySubstitution(basePath, baseProcessFileName, selectedVariants, resultFileName);
			System.out.println("*** FIN activitySubstitution ***");

			LaneSubstitution.laneSubstitution(basePath, resultFileName, selectedVariants, resultFileName);
			System.out.println("*** FIN laneSubstitution ***");

			ActivitySupression.activitySupression(basePath, resultFileName, selectedVariants, resultFileName);
			System.out.println("*** FIN activitySupression ***");

			// Recursión sobre subprocesos
			for (String subProcessName : subprocessResult.keySet()) {
				String subProcessFilePath = subprocessResult.get(subProcessName);
				replace(basePath, subProcessFilePath, selectedVariants, subProcessFilePath);
			}

			// Meter todos los subporcesos en el archivo final
			for (String subProcessName : subprocessResult.keySet()) {
				String subProcessFilePath = subprocessResult.get(subProcessName);
				System.out.println("Substituir " + subProcessName + " por lo que hay en el archivo " + subProcessFilePath);
				SubprocessInsertion.subprocessInsertion(basePath, resultFileName, subProcessName, subProcessFilePath, resultFileName);
				System.out.println("*** FIN subprocessInsertion ***");
			}

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
