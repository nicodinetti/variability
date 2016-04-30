package uy.edu.fing.modeler.variability.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.ViewPluginAction;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SubstitutionDelegate extends ActionDelegate implements IViewActionDelegate {

	@SuppressWarnings("restriction")
	@Override
	public void run(IAction action) {

		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		Object firstElement = ((TreeSelection) ((ViewPluginAction) action).getSelection()).getFirstElement();

		if (firstElement instanceof File) {
			File file = (File) firstElement;
			if ("bpmn".equals(file.getFileExtension())) {
				Map<String, List<String>> files;
				try {
					files = searchFiles(file);
					WizardDialog wizardDialog = new WizardDialog(parent, new MyWizard(file, files));
					if (wizardDialog.open() == Window.OK) {
						System.out.println("Ok pressed");
					} else {
						System.out.println("Cancel pressed");
					}
				} catch (SAXException | IOException | ParserConfigurationException e) {
					// FIXME Poner pensaje de error
					e.printStackTrace();
					failMessage(parent);
				}

			} else {
				failMessage(parent);
			}

		} else {
			failMessage(parent);
		}

	}

	@SuppressWarnings({ "restriction", "resource" })
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
			Stream<Path> list = Files.list(vpPath);
			if (!Files.exists(vpPath) || !Files.isDirectory(vpPath) || list.count() == 0) {
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

	private void failMessage(Shell parent) {
		MessageBox fail = new MessageBox(parent);
		fail.setMessage("Please, select a bpmn file");
		fail.open();
	}

	@Override
	public void init(IViewPart view) {
		System.out.println("soy el " + SubstitutionDelegate.class);
	}

}
