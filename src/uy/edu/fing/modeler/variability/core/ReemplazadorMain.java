package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.log.LogUtils;
import uy.edu.fing.modeler.variability.utils.Utils;
import uy.edu.fing.modeler.variability.utils.YaoqiangUtils;

public class ReemplazadorMain {

	public static final boolean IMPRIMIR_LOG_SUBPROCESS = false;
	public static final boolean PRINT_LOGS = false;

	public static final String DELETE = "DELETE";

	public static void main(String[] args) throws Exception {
		try {

			Map<String, String> selectedVariants = new HashMap<>();
			selectedVariants.put("p6.bpmn/SubProcess_1", "Lane_2");
			//selectedVariants.put("Task_1", "Lane_3"); // Ejemplo de sustitucion
														// de un lane
			//selectedVariants.put("Task_2", "Sub_B.bpmn"); // Ejemplo de
															// sustitucion de
															// una tarea //
															// Poniendo "A.bpmn"
															// funciona, todavia
															// no con
															// Subprocesos
			String basePath = "/home/abrusco/git/variability/test/pruebas/p6";
			// String basePath =
			// "/Users/ndinetti/Desarrollo/sourcecode/variability/test/reemplazador";
			String baseProcessFileName = "p6.bpmn";
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

			LogUtils.log(baseProcessFileName, "--INI-- Comenzando proceso de reemplazo");

			substitution(1, basePath, baseProcessFileName, selectedVariants, resultFileName);

			// Borrar Diagrama
			Document doc = Utils.getDocument(basePath, resultFileName);
			Utils.removeBPMNDiagram(doc);

			Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

			LogUtils.log(baseProcessFileName, "Ini Yaoqiang AutoLayout");
			try {
				YaoqiangUtils.run(basePath, resultFileName);
			} catch (Exception e) {
				LogUtils.log(baseProcessFileName, "Yaoqiang falló");
				e.printStackTrace();
			}
			LogUtils.log(baseProcessFileName, "Fin Yaoqiang AutoLayout");

			LogUtils.log(baseProcessFileName, "--FIN--");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	private static void substitution(int i, String basePath, String baseProcessFileName, Map<String, String> allSelecteds, String resultFileName)
			throws IOException, Exception, SAXException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		Map<String, String> selectedVariants = new HashMap<>();
		for (String key : allSelecteds.keySet()) {
			String[] split = key.split("/");
			if (split.length == i * 2) {
				selectedVariants.put(split[(i * 2) - 1], allSelecteds.get(key));
			}
		}

		LogUtils.logNext(baseProcessFileName, "Ini laneSubstitution");
		LaneSubstitution.laneSubstitution(basePath, baseProcessFileName, selectedVariants, resultFileName);
		LogUtils.logBack(baseProcessFileName, "Fin laneSubstitution");

		LogUtils.logNext(baseProcessFileName, "Ini activitySubstitution");
		Map<String, String> subprocessResult = ActivitySubstitution.activitySubstitution(basePath, resultFileName, selectedVariants, resultFileName);
		LogUtils.logBack(baseProcessFileName, "Fin activitySubstitution");

		LogUtils.logNext(baseProcessFileName, "Ini activitySupression");
		ActivitySupression.activitySupression(basePath, resultFileName, selectedVariants, resultFileName);
		LogUtils.logBack(baseProcessFileName, "Fin activitySupression");

		// Recursión sobre subprocesos
		LogUtils.logNext(baseProcessFileName, "Ini recursión sobre subprocesos");
		for (String subProcessName : subprocessResult.keySet()) {
			String subProcessFilePath = subprocessResult.get(subProcessName);
			LogUtils.logNext(baseProcessFileName, "Ini " + subProcessFilePath);
			substitution(i + 1, basePath, subProcessFilePath, allSelecteds, subProcessFilePath);
			LogUtils.logBack(baseProcessFileName, "Fin " + subProcessFilePath);
		}
		LogUtils.logBack(baseProcessFileName, "Fin recursión sobre subprocesos");

		// Meter todos los subprocesos en el archivo final
		LogUtils.logNext(baseProcessFileName, "Ini armado de archivo XML final");
		for (String subProcessName : subprocessResult.keySet()) {
			String subProcessFilePath = subprocessResult.get(subProcessName);
			LogUtils.logNext(baseProcessFileName, "Ini " + subProcessFilePath);
			SubprocessInsertion.subprocessInsertion(basePath, resultFileName, subProcessName, subProcessFilePath, resultFileName);
			LogUtils.logBack(baseProcessFileName, "Fin " + subProcessFilePath);
		}

		LogUtils.logBack(baseProcessFileName, "Fin armado de archivo XML final");
	}

}
