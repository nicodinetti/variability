package uy.edu.fing.modeler.variability.core;

import java.io.File;
// http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.yaoqiang.bpmn.model.elements.core.infrastructure.Definitions;
import org.yaoqiang.graph.model.GraphModel;
import org.yaoqiang.graph.swing.GraphComponent;
import org.yaoqiang.graph.util.GraphUtils;
import org.yaoqiang.graph.view.Graph;

import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;

import uy.edu.fing.modeler.variability.log.LogUtils;
import uy.edu.fing.modeler.variability.utils.Utils;
import uy.edu.fing.modeler.variability.utils.YaoqiangUtils;

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

			// Borrar Diagrama
			Path filepathBase = Paths.get(basePath + File.separatorChar + resultFileName);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepathBase.toString());
			Utils.removeBPMNDiagram(doc);

			Utils.saveResult(baseProcessFileName, doc, basePath, resultFileName);

			System.out.println("Yaoqiang AutoLayout...");

			Definitions bpmnModel = null;
			Graph graph = new Graph(new GraphModel());
			GraphComponent graphComponent = new GraphComponent(graph);
			YaoqiangUtils codec = new YaoqiangUtils(graph);
			codec.decode(basePath + File.separatorChar + resultFileName);

			if (codec.isAutolayout()) {
				for (Object pool : graph.getAllPools()) {
					GraphUtils.arrangeSwimlaneSize(graph, pool, false, false, false);
				}
				GraphUtils.arrangeSwimlanePosition(graphComponent);
			}
			bpmnModel = graph.getBpmnModel();
			bpmnModel.setName(resultFileName);

			File file = new File(basePath + File.separatorChar + resultFileName);
			file.delete();

			String xml = mxXmlUtils.getXml(codec.encode().getDocumentElement());
			mxUtils.writeFile(xml, basePath + File.separatorChar + resultFileName);

			LogUtils.log(baseProcessFileName, "--FIN--");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
