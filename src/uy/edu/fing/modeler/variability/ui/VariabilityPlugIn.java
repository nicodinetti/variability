package uy.edu.fing.modeler.variability.ui;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uy.edu.fing.modeler.variability.log.LogUtils;
import uy.edu.fing.modeler.variability.ui.model.VarPoint;
import uy.edu.fing.modeler.variability.ui.model.Variant;

@SuppressWarnings("restriction")
public class VariabilityPlugIn extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		Object firstElement = ((TreeSelection) selection).getFirstElement();

		if (firstElement instanceof File) {
			File file = (File) firstElement;
			if ("bpmn".equals(file.getFileExtension())) {
				try {

					String fileName = file.getName();
					String folder = file.getParent().getRawLocation().toString();
					Variant variant = searchVPOptions(folder, fileName);

					Map<String, Properties> configs = searchConfigs(file);
					LogUtils.log(this.getClass().getSimpleName(), "Abriendo Wizard...");
					WizardDialog wizardDialog = new WizardDialog(parent, new MyWizard(file, variant, configs));
					if (wizardDialog.open() == Window.OK) {
					}

				} catch (Exception e) {
					e.printStackTrace();
					failMessage(parent, e.getMessage());
				}

			} else {
				failMessage(parent, "Please, select a bpmn file");
			}

		} else {
			failMessage(parent, "Please, select a bpmn file");
		}
		return null;
	}

	private Map<String, Properties> searchConfigs(File file) throws IOException {
		Map<String, Properties> res = new HashMap<>();

		Path path = Paths.get(file.getParent().getRawLocationURI());
		LogUtils.log(this.getClass().getSimpleName(), "Buscando configuraciones en: " + path.toString());

		List<Path> list = Files.list(path).collect(Collectors.toList());
		for (Path p : list) {
			String pPath = p.toString();
			if (pPath.endsWith(".conf")) {
				Reader reader = new FileReader(pPath);
				Properties prop = new Properties();
				prop.load(reader);
				String configName = p.getFileName().toString();
				res.put(configName, prop);
				LogUtils.log(this.getClass().getSimpleName(), "Configuración cargada: " + configName);
			}
		}

		return res;
	}

	private Variant searchVPOptions(String folder, String varName) throws SAXException, IOException, ParserConfigurationException {
		Variant res = new Variant();
		res.setVarName(varName);

		LogUtils.log(this.getClass().getSimpleName(), "Buscando VPs en " + folder);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document variantDoc = docBuilder.parse(folder + java.io.File.separatorChar + varName);

		List<Node> vpListByType = getVPListByType(variantDoc, "bpmn2:task", "bpmn2:subProcess");
		LogUtils.log(this.getClass().getSimpleName(), "Cantidad de VPs:" + vpListByType.size());

		for (Node node : vpListByType) {
			String vpName = node.getAttributes().getNamedItem("id").getNodeValue();
			String vp = folder + java.io.File.separatorChar + vpName;

			VarPoint varPoint = new VarPoint(vpName);
			LogUtils.log(this.getClass().getSimpleName(), "Buscando variantes para el VP: " + vp);

			Path vpPath = Paths.get(vp);
			if (!Files.exists(vpPath) || !Files.isDirectory(vpPath)) {
				LogUtils.log(this.getClass().getSimpleName(), "No existe la carpeta con las variantes");
				throw new RuntimeException("No existen variantes para el VP: " + vp);
			}

			Stream<Path> list = Files.list(vpPath).filter(x -> !Files.isDirectory(Paths.get(x.toUri())));
			if (list.count() == 0) {
				LogUtils.log(this.getClass().getSimpleName(), "No existen variantes en la carpeta");
				throw new RuntimeException("No existen variantes para el VP: " + vp);
			}

			list = Files.list(vpPath).filter(x -> !Files.isDirectory(Paths.get(x.toUri())));
			List<String> variants = list.map(x -> x.getFileName().toString()).collect(Collectors.toList());

			LogUtils.log(this.getClass().getSimpleName(), vpName + ": Cantidad de variantes encontradas " + variants.size());

			LogUtils.log(this.getClass().getSimpleName(), "Ver si hay variantes a nivel subprocess ");
			for (String vName : variants) {
				LogUtils.log(this.getClass().getSimpleName(), "Variant: " + vName);
				Variant variant = searchVPOptions(vpPath.toString(), vName);
				varPoint.getVarOptions().add(variant);
			}

			res.getVpOptions().add(varPoint);
			LogUtils.log(this.getClass().getSimpleName(), "Variantes a nivel subprocess ");
		}
		return res;
	}

	private static List<Node> getVPListByType(Document doc, String... types) {
		List<Node> variationPoints = new ArrayList<Node>();

		for (String type : types) {

			NodeList nodeList = doc.getElementsByTagName(type);
			LogUtils.log("getVPListByType", "Buscando VPs de tipo: " + type);
			for (int it = 0; it < nodeList.getLength(); it++) {
				Node nodo = nodeList.item(it);
				NamedNodeMap attr = nodo.getAttributes();
				Node nodeAttr = attr.getNamedItem("ext:variability");
				if (nodeAttr != null) {
					LogUtils.log("getVPListByType", "VP encontrado: " + nodo.getNodeName());
					variationPoints.add(nodo);
				}
			}
		}
		return variationPoints;
	}

	public static void failMessage(Shell parent, String message) {
		MessageBox fail = new MessageBox(parent);
		fail.setMessage(message);
		fail.open();
	}
}
