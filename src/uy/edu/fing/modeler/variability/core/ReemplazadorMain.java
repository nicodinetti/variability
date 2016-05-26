package uy.edu.fing.modeler.variability.core;

// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.log.LogUtils;

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

			LogUtils.log(baseProcessFileName, "--INI-- Comenzando proceso de reemplazo");

			LogUtils.logNext(baseProcessFileName, "Ini activitySubstitution");
			Map<String, String> subprocessResult = ActivitySubstitution.activitySubstitution(basePath, baseProcessFileName, selectedVariants, resultFileName);
			LogUtils.logBack(baseProcessFileName, "Fin activitySubstitution");

			LogUtils.logNext(baseProcessFileName, "Ini laneSubstitution");
			LaneSubstitution.laneSubstitution(basePath, resultFileName, selectedVariants, resultFileName);
			LogUtils.logBack(baseProcessFileName, "Fin laneSubstitution");

			LogUtils.logNext(baseProcessFileName, "Ini activitySupression");
			ActivitySupression.activitySupression(basePath, resultFileName, selectedVariants, resultFileName);
			LogUtils.logBack(baseProcessFileName, "Fin activitySupression");

			// Recursión sobre subprocesos
			LogUtils.logNext(baseProcessFileName, "Ini recursión sobre subprocesos");
			for (String subProcessName : subprocessResult.keySet()) {
				String subProcessFilePath = subprocessResult.get(subProcessName);
				LogUtils.logNext(baseProcessFileName, "Ini " + subProcessFilePath);
				replace(basePath, subProcessFilePath, selectedVariants, subProcessFilePath);
				LogUtils.logBack(baseProcessFileName, "Fin " + subProcessFilePath);
			}
			LogUtils.logBack(baseProcessFileName, "Fin recursión sobre subprocesos");

			// Meter todos los subporcesos en el archivo final
			LogUtils.logNext(baseProcessFileName, "Ini armado de archivo XML final");
			for (String subProcessName : subprocessResult.keySet()) {
				String subProcessFilePath = subprocessResult.get(subProcessName);
				LogUtils.logNext(baseProcessFileName, "Ini " + subProcessFilePath);
				SubprocessInsertion.subprocessInsertion(basePath, resultFileName, subProcessName, subProcessFilePath, resultFileName);
				LogUtils.logBack(baseProcessFileName, "Fin " + subProcessFilePath);
			}
			LogUtils.logBack(baseProcessFileName, "Fin armado de archivo XML final");

			LogUtils.log(baseProcessFileName, "--FIN--");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
