package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

public class ReemplazadorMain {

	public static final String DELETE = "DELETE";

	public static void main(String[] args) throws Exception {
		try {

			// String basePath =
			// "/Users/ndinetti/Desarrollo/sourcecode/reemplazador/Reemplazador/src";
			String basePath = "/home/abrusco/git/reemplazador/Reemplazador/src";
			// String baseProcessFileName = "test_lane.bpmn";
			String baseProcessFileName = "test_lane.bpmn";
			String resultFileName = "result_lane.bpmn";
			Map<String, String> selectedVariants = new HashMap<>();
			// selectedVariants.put("Task_1", "S1.bpmn");
			selectedVariants.put("Task_1", "Lane_3");
			// selectedVariants.put("SubProcess_1", "T1.bpmn");

			/*
			 * String baseProcessFileName = "process_1.bpmn"; //String
			 * baseProcessFileName = "SubProcess_1/C.bpmn"; String
			 * resultFileName = "result.bpmn"; Map<String, String>
			 * selectedVariants = new HashMap<>();
			 * //selectedVariants.put("Task_1", ActivitySupression.DELETE);
			 * selectedVariants.put("Task_1", "A.bpmn");
			 * selectedVariants.put("SubProcess_1", "D.bpmn");
			 */

			ActivitySubstitution.activitySubstitution(basePath, baseProcessFileName, selectedVariants, resultFileName);
			LaneSubstitution.substitution(basePath, resultFileName, selectedVariants, resultFileName);
			ActivitySupression.activitySupression(basePath, resultFileName, selectedVariants, resultFileName);

			System.out.println("Done");

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

	public static boolean replace(String basePath, String baseProcessFileName, Map<String, String> selectedVariants, String resultFileName) {

		try {
			ActivitySubstitution.activitySubstitution(basePath, baseProcessFileName, selectedVariants, resultFileName);
			LaneSubstitution.substitution(basePath, resultFileName, selectedVariants, resultFileName);
			ActivitySupression.activitySupression(basePath, resultFileName, selectedVariants, resultFileName);

			System.out.println("Done");
			return true;

		} catch (TransformerFactoryConfigurationError | Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
