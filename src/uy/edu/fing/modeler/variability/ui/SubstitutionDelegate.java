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

@SuppressWarnings("restriction")
public class SubstitutionDelegate extends AbstractHandler {

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
				Map<String, List<String>> files;
				try {
					files = searchFiles(file);
					Map<String, Properties> configs = searchConfigs(file);
					WizardDialog wizardDialog = new WizardDialog(parent, new MyWizard(file, files, configs));
					if (wizardDialog.open() == Window.OK) {
						System.out.println("Ok pressed");
					} else {
						System.out.println("Cancel pressed");
					}
				} catch (SAXException | IOException | ParserConfigurationException e) {
					// FIXME Poner mensaje de error
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

		List<Path> list = Files.list(Paths.get(file.getParent().getRawLocationURI())).collect(Collectors.toList());
		for (Path path : list) {
			if (path.toString().endsWith(".conf")) {
				Reader reader = new FileReader(path.toString());
				Properties prop = new Properties();
				prop.load(reader);
				res.put(path.getFileName().toString(), prop);
			}
		}

		return res;
	}

	@SuppressWarnings("resource")
	private Map<String, List<String>> searchFiles(File file) throws SAXException, IOException, ParserConfigurationException {
		Map<String, List<String>> res = new HashMap<>();

		String path = file.getRawLocation().toString();

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document variantDoc = docBuilder.parse(path);

		List<Node> vpListByType = getVPListByType(variantDoc, "bpmn2:task", "bpmn2:subProcess");

		String parentFullPath = file.getParent().getRawLocation().toString();
		for (Node node : vpListByType) {
			String vpName = node.getAttributes().getNamedItem("id").getNodeValue();
			Path vpPath = Paths.get(parentFullPath + java.io.File.separatorChar + vpName);
			if (!Files.exists(vpPath) || !Files.isDirectory(vpPath)) {
				throw new RuntimeException("No existen variantes para el VP: " + vpName);
			}

			Stream<Path> list = Files.list(vpPath);
			if (list.count() == 0) {
				throw new RuntimeException("No existen variantes para el VP: " + vpName);
			}

			list = Files.list(vpPath);
			res.put(vpName, list.map(x -> x.getFileName().toString()).collect(Collectors.toList()));
		}
		return res;
	}

	private static List<Node> getVPListByType(Document doc, String... types) {
		List<Node> variationPoints = new ArrayList<Node>();

		for (String type : types) {

			NodeList nodeList = doc.getElementsByTagName(type);
			for (int it = 0; it < nodeList.getLength(); it++) {
				Node nodo = nodeList.item(it);
				NamedNodeMap attr = nodo.getAttributes();
				Node nodeAttr = attr.getNamedItem("ext:variability");
				if (nodeAttr != null) {
					variationPoints.add(nodo);
				}
			}
		}
		return variationPoints;
	}

	private void failMessage(Shell parent, String message) {
		MessageBox fail = new MessageBox(parent);
		fail.setMessage(message);
		fail.open();
	}
}
