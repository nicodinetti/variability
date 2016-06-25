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
import uy.edu.fing.modeler.variability.ui.model.ModelVariant;

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
					ModelVariant variant = searchVPOptions(folder, fileName.replace(".bpmn", ""));

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
				fix(prop);
				String configName = p.getFileName().toString();
				res.put(configName, prop);
				LogUtils.log(this.getClass().getSimpleName(), "Configuración cargada: " + configName);
			}
		}

		return res;
	}

	private void fix(Properties prop) {

	}

	private ModelVariant searchVPOptions(String folder, String file) throws SAXException, IOException, ParserConfigurationException {
		ModelVariant res = new ModelVariant();
		res.setVarpointName(file);
		res.setFileName(folder + java.io.File.separatorChar + file + ".bpmn");

		LogUtils.log(this.getClass().getSimpleName(), "Buscando VPs en " + file);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document variantDoc = docBuilder.parse(folder + java.io.File.separatorChar + file + ".bpmn");

		List<Node> vpList = getVPListByType(variantDoc, "bpmn2:task", "bpmn2:subProcess");
		LogUtils.log(this.getClass().getSimpleName(), "Cantidad de VPs:" + vpList.size());

		for (Node node : vpList) {
			String vpName = node.getAttributes().getNamedItem("id").getNodeValue();
			String vpFolder = folder + java.io.File.separatorChar + "varPoint(" + vpName + ")";

			Path folderPath = getFolder(vpName, vpFolder);

			ModelVariant varPoint = new ModelVariant();
			varPoint.setVarpointName(vpName);
			LogUtils.log(this.getClass().getSimpleName(), "Buscando variantes para el VP: " + vpName);

			List<Path> paths = Files.list(folderPath).collect(Collectors.toList());
			LogUtils.log(this.getClass().getSimpleName(), vpName + ": Cantidad de variantes encontradas " + paths.size());

			LogUtils.log(this.getClass().getSimpleName(), "Ver si hay variantes a nivel subprocess ");
			for (Path vName : paths) {
				LogUtils.log(this.getClass().getSimpleName(), "Variant: " + vName);
				if (Files.isDirectory(vName)) {
					ModelVariant variant = searchVPOptions(vpFolder + java.io.File.separatorChar + vName.getFileName().toString(), vName.getFileName().toString());
					varPoint.getModelVariants().add(variant);

				} else {
					ModelVariant variant = new ModelVariant();
					variant.setVarpointName(vName.getFileName().toString());
					variant.setFileName(vName.toString());
					varPoint.getModelVariants().add(variant);
				}
			}

			res.getModelVariants().add(varPoint);
			LogUtils.log(this.getClass().getSimpleName(), "Variantes a nivel subprocess ");
		}
		return res;
	}

	@SuppressWarnings("resource")
	private Path getFolder(String vpName, String vpFolder) throws IOException {
		Path path = Paths.get(vpFolder);
		if (!Files.exists(path) || !Files.isDirectory(path)) {
			LogUtils.log(this.getClass().getSimpleName(), "No existe la carpeta con las variantes para " + vpName);
			throw new RuntimeException("No existen variantes para el VP: " + path);
		}

		Stream<Path> list = Files.list(path);
		if (list.count() == 0) {
			LogUtils.log(this.getClass().getSimpleName(), "No existen variantes en la carpeta");
			throw new RuntimeException("No existen variantes para el VP: " + path);
		}
		return path;
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
