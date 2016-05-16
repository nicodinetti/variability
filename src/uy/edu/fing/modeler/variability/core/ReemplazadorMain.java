package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class ReemplazadorMain {
	
	public static final String SUBPROCESS_ID = "Task_1";
	public static final String START_EVENT_SUBPROCESS_ID = "StartEvent_10";
	public static final String DELETE = "DELETE";
	private static boolean primero = true;
	private static String BASE_PROCESS_FILE_NAME = "";
	private static String RESULT_FILE_NAME = "";
	public static final boolean IMPRIMIR_LOG_SUBPROCESS = false;
	
	public static void main(String[] args) throws Exception {
		try {

			Map<String, String> selectedVariants = new HashMap<>();
			selectedVariants.put(SUBPROCESS_ID, "S2.bpmn");
			
			String basePath = "/home/abrusco/git/variability/test/reemplazador";
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

	private static String getResultFileName() {
		if (primero) {
			primero = false;
			return BASE_PROCESS_FILE_NAME;
		}
		return RESULT_FILE_NAME;
	}

	public static void replace(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName) throws Exception {

		try {
			BASE_PROCESS_FILE_NAME = baseProcessFileName;
			RESULT_FILE_NAME = resultFileName;

			Map<String, String> subprocessResult = ActivitySubstitution.activitySubstitution(basePath, getResultFileName(), selectedVariants, resultFileName);
			System.out.println("*** FIN activitySubstitution ***");
			LaneSubstitution.laneSubstitution(basePath, getResultFileName(), selectedVariants, resultFileName);
			System.out.println("*** FIN laneSubstitution ***");
			ActivitySupression.activitySupression(basePath, getResultFileName(), selectedVariants, resultFileName);
			System.out.println("*** FIN activitySupression ***");

			for (String subProcessName : subprocessResult.keySet()) {
				String subProcessFilePath = subprocessResult.get(subProcessName);
				System.out.println("Substituir " + subProcessName + " por lo que hay en el archivo " + subProcessFilePath);
				SubprocessInsertion.subprocessInsertion(basePath, getResultFileName(), subProcessName, subProcessFilePath, resultFileName);
				System.out.println("*** FIN subprocessInsertion ***");
			}

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
