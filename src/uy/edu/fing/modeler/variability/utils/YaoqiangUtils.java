package uy.edu.fing.modeler.variability.utils;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaoqiang.bpmn.model.BPMNModelConstants;
import org.yaoqiang.bpmn.model.BPMNModelEntityResolver;
import org.yaoqiang.bpmn.model.BPMNModelParsingErrors;
import org.yaoqiang.bpmn.model.BPMNModelParsingErrors.ErrorMessage;
import org.yaoqiang.bpmn.model.BPMNModelUtils;
import org.yaoqiang.bpmn.model.elements.XMLAttribute;
import org.yaoqiang.bpmn.model.elements.XMLComplexElement;
import org.yaoqiang.bpmn.model.elements.XMLElement;
import org.yaoqiang.bpmn.model.elements.XMLExtensionElement;
import org.yaoqiang.bpmn.model.elements.activities.CallActivity;
import org.yaoqiang.bpmn.model.elements.artifacts.Artifacts;
import org.yaoqiang.bpmn.model.elements.artifacts.TextAnnotation;
import org.yaoqiang.bpmn.model.elements.bpmndi.BPMNDiagram;
import org.yaoqiang.bpmn.model.elements.bpmndi.BPMNPlane;
import org.yaoqiang.bpmn.model.elements.collaboration.Collaboration;
import org.yaoqiang.bpmn.model.elements.core.foundation.BaseElement;
import org.yaoqiang.bpmn.model.elements.core.infrastructure.Definitions;
import org.yaoqiang.bpmn.model.elements.process.BPMNProcess;
import org.yaoqiang.graph.io.bpmn.BPMNCodec;
import org.yaoqiang.graph.io.bpmn.BPMNCodecUtils;
import org.yaoqiang.graph.layout.BPMNLayout;
import org.yaoqiang.graph.model.GraphModel;
import org.yaoqiang.graph.swing.GraphComponent;
import org.yaoqiang.graph.util.Constants;
import org.yaoqiang.graph.util.GraphUtils;
import org.yaoqiang.graph.view.Graph;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxImageBundle;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxCellState;

import uy.edu.fing.modeler.variability.log.LogUtils;

public class YaoqiangUtils extends BPMNCodec {

	private static final String NAME_PLUGIN = "BPMNext-";

	private double maxLaneWidth = 0D;

	public YaoqiangUtils(Graph graph) {
		super(graph);
	}

	public static void run(String basePath, String resultFileName) throws Exception {
		Definitions bpmnModel = null;
		Graph graph = new Graph(new GraphModel());
		GraphComponent graphComponent = new GraphComponent(graph);
		YaoqiangUtils codec = new YaoqiangUtils(graph);
		List<ErrorMessage> errors = codec.decode(basePath + File.separatorChar + resultFileName);

		if (!errors.isEmpty()) {
			String reduce = errors.stream().map(x -> x.getType() + "-" + x.getMessage()).reduce("", (x, y) -> x + y);
			throw new Exception(reduce);
		}

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
	}

	@Override
	public List<ErrorMessage> decode(Object file) {

		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();

		mxCell root = (mxCell) model.createRoot();
		graph.clearBpmnModel();
		model.setRoot(root);

		Document doc = parseDocument(file, true, errorMessages);
		if (doc == null || errorMessages.size() > 0) {
			LogUtils.log("", "Not a valid BPMN file!");
			return errorMessages;
		}

		bpmnCodec.decode(doc.getDocumentElement(), bpmnModel);

		Artifacts artifacts = BPMNModelUtils.getArtifacts(bpmnModel, false);
		if (artifacts != null) {
			Set<XMLExtensionElement> customArtifactEls = new HashSet<XMLExtensionElement>();
			XMLExtensionElement extensionElements = ((BaseElement) artifacts.getParent()).getExtensionElements();
			for (XMLExtensionElement el : extensionElements.getChildElements("yaoqiang:artifact")) {
				customArtifactEls.add(el);
				String name = el.getAttribute("name").toValue();
				String artifact = graph.getImageFromBundles(name);
				if (artifact == null && el.getAttribute("image") != null) {
					mxImageBundle bundle = new mxImageBundle();
					bundle.putImage(name, el.getAttribute("image").toValue());
					graph.addImageBundle(bundle);
				}
			}
			if (!customArtifactEls.isEmpty()) {
				for (XMLExtensionElement ca : customArtifactEls) {
					extensionElements.removeChildElement(ca);
				}
			}
		}

		bpmnElementMap = bpmnCodec.getBPMNElementMap();
		setNamespaces();
		graph.setBpmnElementMap(bpmnElementMap);

		mxCell defParent = (mxCell) model.getChildAt(root, 0);

		BPMNDiagram diagram = bpmnModel.getFirstBPMNDiagram();
		List<XMLElement> shapes = new ArrayList<XMLElement>();
		List<XMLElement> edges = new ArrayList<XMLElement>();
		if (diagram != null) {
			if (diagram.getName().length() != 0 && defParent.getValue() instanceof String) {
				defParent.setValue(diagram.getName());
			}

			String id = diagram.getBPMNPlane().getBpmnElement();
			if (id.equals("_0")) {
				XMLComplexElement bpmnElement = (XMLComplexElement) bpmnElementMap.get(id);
				id = "_" + id;
				bpmnElement.set("id", id);
				bpmnElementMap.put(id, bpmnElement);
				diagram.getBPMNPlane().setBpmnElement(id);
			}
			shapes = diagram.getBPMNPlane().getBPMNShapes();
			edges = diagram.getBPMNPlane().getBPMNEdges();

			BPMNCodecUtils.cleanupDiagram(bpmnModel, bpmnElementMap);
		}

		if (diagram == null || shapes.isEmpty() && edges.isEmpty()) {
			autolayout = true;
			Collaboration collaboration = BPMNModelUtils.getCollaboration(graph.getBpmnModel());
			if (collaboration != null) {
				BPMNModelUtils.generateBPMNDI(graph.getBpmnModel(), collaboration, bpmnElementMap, shapes, edges);
			}
			BPMNProcess process = BPMNModelUtils.getDefaultProcess(graph.getBpmnModel());
			if (process != null) {
				BPMNModelUtils.generateBPMNDI(process, bpmnElementMap, shapes, edges);
			}
		}

		mxPoint pageSize = decodeBPMNShapes(shapes, defParent);
		decodeBPMNEdges(edges, defParent);
		model.setRoot(root);

		setPageSize(diagram, pageSize);

		if (autolayout) {
			mxIGraphLayout bpmnLayout = new BPMNLayout(graph,
					"1".equals(Constants.SETTINGS.getProperty("orientation", "1")));
			if (bpmnLayout instanceof BPMNLayout) {
				int nodeDistance = Integer.parseInt(Constants.SETTINGS.getProperty("nodeDistance", "20"));
				int levelDistance = Integer.parseInt(Constants.SETTINGS.getProperty("levelDistance", "40"));
				((BPMNLayout) bpmnLayout).setNodeDistance(nodeDistance);
				((BPMNLayout) bpmnLayout).setLevelDistance(levelDistance);
			}
			List<Object> lanesAndSubprocesses = graph.getAllLanesAndSubprocesses();
			model.beginUpdate();
			for (Object cell : lanesAndSubprocesses) {
				bpmnLayout.execute(cell);
			}
			model.endUpdate();
		}

		if (bpmnModel.getBPMNDiagrams().size() > 1) {
			for (XMLElement el : bpmnModel.getBPMNDiagrams().getXMLElements()) {
				if (el == diagram) {
					continue;
				}
				BPMNPlane plane = ((BPMNDiagram) el).getBPMNPlane();
				String id = plane.getBpmnElement();
				if (id.equals("_1") || id.equals("_0")) {
					XMLComplexElement bpmnElement = (XMLComplexElement) bpmnElementMap.get(id);
					id = "_" + id;
					bpmnElement.set("id", id);
					bpmnElementMap.put(id, bpmnElement);
					plane.setBpmnElement(id);
				}
				shapes = plane.getBPMNShapes();
				edges = plane.getBPMNEdges();
				defParent = (mxCell) model.getCell(id);
				if (defParent == null) {
					defParent = new mxCell();
					defParent.setId(id);
					root.insert(defParent, root.getChildCount());
				} else if (bpmnElementMap.get(id) instanceof CallActivity) {
					CallActivity bpmnElement = (CallActivity) bpmnElementMap.get(id);
					defParent = new mxCell();
					defParent.setId(bpmnElement.getCalledElement());
					root.insert(defParent, root.getChildCount());
					plane.setBpmnElement(bpmnElement.getCalledElement());
				} else {
					LogUtils.log("",
							"Yaoqiang BPMN Editor does not support the Sub-Process defined in a separate diagram");
					return errorMessages;
				}
				defParent.setValue(((BPMNDiagram) el).getName());
				decodeBPMNShapes(shapes, defParent);
				decodeBPMNEdges(edges, defParent);
				model.setRoot(root);
			}
		}

		return errorMessages;
	}

	private void setNamespaces() {
		if (!bpmnModel.getNamespaces().containsValue("tns")) {
			if (bpmnModel.getTargetNamespace().length() == 0) {
				if (bpmnModel.getId().length() == 0) {
					bpmnModel.setId("_" + System.currentTimeMillis());
				}
				bpmnModel.setTargetNamespace(BPMNModelConstants.BPMN_TARGET_MODEL_NS + bpmnModel.getId());
				bpmnModel.getNamespaces().put(bpmnModel.getTargetNamespace(), "tns");
			} else {
				bpmnModel.getNamespaces().put(bpmnModel.getTargetNamespace(), "tns");
			}
		}

		bpmnModel.getNamespaces().remove(BPMNModelConstants.XMLNS_XSI);
		bpmnModel.getNamespaces().put(BPMNModelConstants.XMLNS_XSI, "xsi");
	}

	private void setPageSize(BPMNDiagram diagram, mxPoint pageSize) {
		if (diagram != null && diagram.getDocumentation().startsWith("background=")) {
			Map<String, Object> props = graph.getStylesheet().getCellStyle(diagram.getDocumentation(), null);
			if (props != null) {
				if (props.get("background") != null) {
					Color backgroundColor = mxUtils.parseColor(props.get("background").toString());
					model.setBackgroundColor(backgroundColor);
				}
				if (props.get("count") != null) {
					model.setPageCount(Integer.parseInt(props.get("count").toString()));
				}
				if (props.get("horizontalcount") != null) {
					model.setHorizontalPageCount(Integer.parseInt(props.get("horizontalcount").toString()));
				}
			}

			PageFormat pageFormat = new PageFormat();
			Paper paper = new Paper();
			if (props != null && props.get("orientation") != null && props.get("width") != null
					&& props.get("height") != null) {
				int orientation = Integer.parseInt(props.get("orientation").toString());
				double width = Double.parseDouble(props.get("width").toString());
				double height = Double.parseDouble(props.get("height").toString());
				pageFormat.setOrientation(orientation);
				paper.setSize(width, height);
				if (props.get("imageableWidth") != null && props.get("imageableHeight") != null) {
					double x = Double.parseDouble(props.get("imageableX").toString());
					double y = Double.parseDouble(props.get("imageableY").toString());
					double imageableWidth = Double.parseDouble(props.get("imageableWidth").toString());
					double imageableHeight = Double.parseDouble(props.get("imageableHeight").toString());
					paper.setImageableArea(x, y, imageableWidth, imageableHeight);
				}
			} else {
				paper.setSize(Constants.PAGE_HEIGHT, Constants.PAGE_WIDTH);
			}
			pageFormat.setPaper(paper);
			model.setPageFormat(pageFormat);

			Constants.SWIMLANE_WIDTH = (int) (model.getPageFormat().getWidth() * 1.25
					+ (model.getHorizontalPageCount() - 1)
							* (Constants.SWIMLANE_START_POINT + model.getPageFormat().getWidth() * 1.25));

			Constants.SWIMLANE_HEIGHT = (int) (model.getPageFormat().getHeight() * 1.2 + (model.getPageCount() - 1)
					* (Constants.SWIMLANE_START_POINT + model.getPageFormat().getHeight() * 1.2));

		} else {
			int horizontalPageCount = (int) Math.round(pageSize.getX() / Constants.PAGE_WIDTH);
			int pageCount = (int) Math.round(pageSize.getY() / Constants.PAGE_HEIGHT);
			model.setPageCount(Math.max(pageCount, 1));
			model.setHorizontalPageCount(Math.max(horizontalPageCount, 1));
		}
	}

	public static Document parseDocument(Object toParse, boolean isFile, List<ErrorMessage> errorMessages) {

		Document document = null;

		try {
			String xsdDir = new File(YaoqiangUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath())
					.getAbsolutePath() + File.separatorChar + "resources" + File.separatorChar;
			
			if (!new File(xsdDir + "BPMN20.xsd").exists()) {
				LogUtils.log("Yaoqiang","File doesnt exists: " + xsdDir);
				xsdDir = "classpath:/resources/";
				LogUtils.log("Yaoqiang","Getting file from JAR: " + xsdDir);
			}

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setValidating(true);
			docBuilderFactory.setNamespaceAware(true);
			docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
					xsdDir + "BPMN20.xsd");

			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			BPMNModelParsingErrors pErrors = new BPMNModelParsingErrors();
			docBuilder.setErrorHandler(pErrors);
			docBuilder.setEntityResolver(new BPMNModelEntityResolver());
			if (isFile) {
				String filepath = toParse.toString();
				File f = new File(filepath);
				if (!f.exists()) {
					URL url = BPMNModelUtils.class.getResource(filepath);
					if (url == null) {
						if (filepath.startsWith("http") || filepath.startsWith("ftp")) {
							url = new URL(filepath);
						}
					}
					if (url != null) {
						document = docBuilder.parse(url.openStream());
					} else {
						document = docBuilder.parse(new InputSource(new StringReader(toParse.toString())));
					}
				} else {
					if (filepath.endsWith(".gz")) {
						document = docBuilder.parse(new GZIPInputStream(new FileInputStream(f)));
					} else {
						document = docBuilder.parse(new FileInputStream(f));
					}
				}
			} else {
				if (toParse instanceof String) {
					document = docBuilder.parse(new InputSource(new StringReader(toParse.toString())));
				} else if (toParse instanceof InputStream) {
					document = docBuilder.parse((InputStream) toParse);
				}
			}
			errorMessages.addAll(pErrors.getErrorMessages());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return document;
	}

	@Override
	public Document encode() {
		Document doc = mxDomUtils.createDocument();

		Set<String> customArtifacts = new HashSet<String>();
		Set<XMLExtensionElement> customArtifactEls = new HashSet<XMLExtensionElement>();
		Artifacts artifacts = BPMNModelUtils.getArtifacts(bpmnModel, false);
		if (artifacts != null) {
			for (XMLElement a : artifacts.getXMLElements()) {
				if (a instanceof TextAnnotation && "image/png".equals(((TextAnnotation) a).getTextFormat())) {
					String artifact = ((TextAnnotation) a).getText();
					XMLElement nameEl = ((TextAnnotation) a).get("yaoqiang:name");
					if (nameEl != null) {
						artifact = nameEl.toValue();
					}
					customArtifacts.add(artifact);
				}
			}
			if (!customArtifacts.isEmpty()) {
				XMLExtensionElement extensionElements = ((BaseElement) artifacts.getParent()).getExtensionElements();
				for (String ca : customArtifacts) {
					if (graph.getImageFromBundles(ca) == null) {
						continue;
					}
					XMLExtensionElement artifactElement = new XMLExtensionElement(extensionElements,
							"yaoqiang:artifact");
					artifactElement.addAttribute(new XMLAttribute(artifactElement, "name", ca));
					artifactElement
							.addAttribute(new XMLAttribute(artifactElement, "image", graph.getImageFromBundles(ca)));
					extensionElements.addChildElement(artifactElement);
					customArtifactEls.add(artifactElement);
				}
			}

		}
		Element defs = bpmnCodec.encode(doc, bpmnModel);
		if (!customArtifactEls.isEmpty()) {
			XMLExtensionElement extensionElements = ((BaseElement) artifacts.getParent()).getExtensionElements();
			for (XMLExtensionElement ca : customArtifactEls) {
				extensionElements.removeChildElement(ca);
			}
		}

		if (bpmndiPrefix.length() == 0) {
			defs.setAttribute("xmlns", BPMNModelConstants.BPMN_DI_NS);
		}

		try {
			Object currentRoot = graph.getCurrentRoot();
			for (Object r : mxGraphModel.getChildren(model, model.getRoot())) {
				if (model.getChildCount(r) > 0) {
					encodeBPMNDiagram(defs, (mxCell) r);
				}
			}
			graph.getView().setCurrentRoot(currentRoot);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getStackTrace(),
					"Please Capture This Error Screen Shots and Submit this BUG.", JOptionPane.ERROR_MESSAGE);
		}

		return doc;
	}

	@Override
	public void encodeBPMNDiagram(Element defs, mxCell root) {

		Element diagram = defs.getOwnerDocument().createElement(bpmndiPrefix + "BPMNDiagram");
		Element plane = defs.getOwnerDocument().createElement(bpmndiPrefix + "BPMNPlane");
		diagram.appendChild(plane);
		defs.appendChild(diagram);

		if (root == model.getChildAt(model.getRoot(), 0)) {
			// PageFormat pageFormat = model.getPageFormat();
			// Paper paper = pageFormat.getPaper();
			// String documentation = "background=" +
			// mxUtils.hexString(model.getBackgroundColor()) + ";count=" +
			// model.getPageCount() + ";horizontalcount="
			// + model.getHorizontalPageCount() + ";orientation=" +
			// pageFormat.getOrientation() + ";width=" + paper.getWidth() +
			// ";height="
			// + paper.getHeight() + ";imageableWidth=" +
			// paper.getImageableWidth() + ";imageableHeight=" +
			// paper.getImageableHeight() + ";imageableX="
			// + paper.getImageableX() + ";imageableY=" + paper.getImageableY();
			//
			// diagram.setAttribute("documentation", documentation);
			BaseElement container = BPMNModelUtils.getDefaultFlowElementsContainer(graph.getBpmnModel());
			plane.setAttribute("bpmnElement", container == null ? root.getId() : container.getId());
		} else {
			plane.setAttribute("bpmnElement", root.getId());
		}

		diagram.setAttribute("id", NAME_PLUGIN + root.getId());
		diagram.setAttribute("name", (String) root.getValue());

		String resolution = "96.0";
		BPMNDiagram bpmnDiagram = bpmnModel.getFirstBPMNDiagram();
		if (bpmnDiagram != null && bpmnDiagram.getResolution().length() > 0) {
			resolution = bpmnDiagram.getResolution();
		}
		diagram.setAttribute("resolution", resolution);
		if (graph.getView().getCurrentRoot() != null && graph.getView().getCurrentRoot() != root) {
			graph.getView().setCurrentRoot(root);
		}

		List<Object> vertices = new ArrayList<Object>();
		List<Object> callActivities = new ArrayList<Object>();
		GraphUtils.getAllVerticesInOrder(graph, root, vertices, callActivities);
		List<Object> edges = model.getAllEdgesInOrder(root, callActivities);
		encodeBPMNShapes(plane, vertices);
		encodeBPMNEdges(plane, edges);
	}

	@Override
	public void encodeBPMNShapes(Element plane, List<Object> vertices) {
		Element el = null;
		Element label = null;
		Element bounds = null;
		Element labelBounds = null;

		for (Object v : vertices) {
			if (model.isChoreographyTask(v) || model.isChoreographySubprocess(v)) {
				continue;
			}

			mxCell cell = (mxCell) v;
			String cellId = cell.getId();
			el = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNShape");
			if (graph.isChoreography(cell) || graph.isSubChoreography(cell)) {
				if (cellId.endsWith("_act")) {
					cellId = cellId.substring(0, cellId.length() - 4);
				} else {
					if (graph.isChoreography(cell)) {
						cellId = cellId + "_CT";
					} else {
						cellId = cellId + "_SC";
					}
				}
				el.setAttribute("id", NAME_PLUGIN + cellId);
				el.setAttribute("bpmnElement", cellId);
			} else if (model.isChoreographyParticipant(cell)) {
				int index = cellId.indexOf("_part_");
				String pShapeId = cellId.substring(0, index);
				el.setAttribute("id", NAME_PLUGIN + cellId);
				el.setAttribute("bpmnElement", cellId.substring(index + 6));
				el.setAttribute("choreographyActivityShape", pShapeId);
				String init = "_non";
				if (model.isInitiatingChoreographyParticipant(cell)) {
					init = "";
				}
				if (graph.isAdditionalChoreographyParticipant(cell)) {
					el.setAttribute("participantBandKind", "middle" + init + "_initiating");
				} else if (graph.isTopChoreographyParticipant(cell)) {
					el.setAttribute("participantBandKind", "top" + init + "_initiating");
				} else if (graph.isBottomChoreographyParticipant(cell)) {
					el.setAttribute("participantBandKind", "bottom" + init + "_initiating");
				}
			} else {
				el.setAttribute("id", NAME_PLUGIN + cellId);
				el.setAttribute("bpmnElement", cellId);
			}
			if (graph.isSwimlane(cell)) {
				el.setAttribute("isHorizontal", Boolean.toString(!graph.isVerticalSwimlane(cell)));
				el.setAttribute("isExpanded", Boolean.toString(!graph.isCollapsedSwimlane(cell)));
			} else if (model.isSubProcess(cell)) {
				el.setAttribute("isExpanded", Boolean.toString(model.isExpandedSubProcess(cell)));
			} else if (graph.isSubChoreography(cell)) {
				el.setAttribute("isExpanded",
						Boolean.toString(model.isExpandedSubProcess(GraphUtils.getChoreographyActivity(model, cell))));
			} else if (model.isCallProcess(cell)) {
				el.setAttribute("isExpanded", "false");
			} else if (cell.getStyle().startsWith("exclusiveGatewayWithIndicator")) {
				el.setAttribute("isMarkerVisible", "true");
			} else if (cell.getStyle().startsWith("exclusiveGateway")) {
				el.setAttribute("isMarkerVisible", "false");
			}
			bounds = plane.getOwnerDocument().createElement(dcPrefix + "Bounds");
			BPMNCodecUtils.setBounds(graph, cell, bounds);
			el.appendChild(bounds);

			label = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNLabel");
			labelBounds = plane.getOwnerDocument().createElement(dcPrefix + "Bounds");
			BPMNCodecUtils.setLabelBounds(graph, cell, labelBounds);
			label.appendChild(labelBounds);
			el.appendChild(label);

			plane.appendChild(el);

			System.out.println("id -> " + cell.getId());
			System.out.println("state -> " + graph.getView().getState(cell));
			System.out.println("geometry -> " + cell.getGeometry());
		}
	}

	@Override
	public void encodeBPMNEdges(Element plane, List<Object> edges) {
		Element el = null;
		Element label = null;
		Element bounds = null;
		Element labelBounds = null;

		List<mxCell> associations = new ArrayList<mxCell>();

		for (Object e : edges) {
			mxCell cell = (mxCell) e;
			if (model.isAssociation(cell)) {
				associations.add(cell);
				continue;
			}
			if (cell.getSource() == null || cell.getTarget() == null) {
				continue;
			}
			el = generateEdgeElement(graph, cell, plane, bpmndiPrefix, dcPrefix, diPrefix);
			if (model.isMessageFlow(cell) && model.getChildCount(cell) > 0) {
				mxCell message = (mxCell) model.getChildAt(cell, 0);
				String messageId = message.getId();
				if (graph.isInitiatingMessage(message)) {
					el.setAttribute("messageVisibleKind", "initiating");
				} else {
					el.setAttribute("messageVisibleKind", "non_initiating");
				}

				Element msgShape = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNShape");
				msgShape.setAttribute("id", NAME_PLUGIN + messageId);
				msgShape.setAttribute("bpmnElement", messageId);
				bounds = plane.getOwnerDocument().createElement(dcPrefix + "Bounds");
				BPMNCodecUtils.setBounds(graph, message, bounds);
				msgShape.appendChild(bounds);
				label = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNLabel");
				labelBounds = plane.getOwnerDocument().createElement(dcPrefix + "Bounds");
				BPMNCodecUtils.setLabelBounds(graph, message, labelBounds);
				label.appendChild(labelBounds);
				msgShape.appendChild(label);
				plane.appendChild(msgShape);
			}
			plane.appendChild(el);

		}

		for (mxCell e : associations) {
			el = generateEdgeElement(graph, e, plane, bpmndiPrefix, dcPrefix, diPrefix);
			plane.appendChild(el);
		}
	}

	private static Element generateEdgeElement(Graph graph, mxCell cell, Element plane, String bpmndiPrefix,
			String dcPrefix, String diPrefix) {
		Element el = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNEdge");
		el.setAttribute("id", NAME_PLUGIN + cell.getId());
		el.setAttribute("bpmnElement", cell.getId());
		el.setAttribute("sourceElement", NAME_PLUGIN + cell.getSource().getId());
		el.setAttribute("targetElement", NAME_PLUGIN + cell.getTarget().getId());

		BPMNCodecUtils.generateWaypoints(diPrefix, el, graph, cell);

		Element label = plane.getOwnerDocument().createElement(bpmndiPrefix + "BPMNLabel");
		Element labelBounds = plane.getOwnerDocument().createElement(dcPrefix + "Bounds");
		BPMNCodecUtils.setLabelBounds(graph, cell, labelBounds);
		label.appendChild(labelBounds);
		el.appendChild(label);

		return el;
	}

	public void arrangeSwimlaneSize(final Graph graph, Object cell, boolean other) {
		final GraphModel model = graph.getModel();
		mxCell swimlane = (mxCell) cell;

		if (!other) {
			Collection<Object> swimlanes = mxGraphModel.filterDescendants(model, new mxGraphModel.Filter() {
				@Override
				public boolean filter(Object cell) {
					return !model.isCollapsed(model.getParentPool(cell))
							&& (graph.hasChildNonLane(cell) || graph.isEmptySwimlane(cell));
				}
			});

			for (Object lane : swimlanes) {
				arrangeSwimlaneSize(graph, lane, true);
			}
		}

		mxCell laneIt = swimlane;

		if (graph.isSwimlane(laneIt)) {
			Object[] cells = mxGraphModel.getChildVertices(model, laneIt);

			double sizeWidth = 0;
			double sizeHeight = 0;

			for (int i = 0; i < cells.length; i++) {
				if (other && !graph.isSwimlane(cells[i])) {
					sizeWidth += model.getGeometry(cells[i]).getWidth();
				} else {
					sizeHeight += model.getGeometry(cells[i]).getHeight();
				}
			}

			mxGeometry geo = model.getGeometry(laneIt);
			mxCellState state = graph.getView().getState(laneIt);

			if (sizeHeight > 0) {
				geo.setHeight(sizeHeight);
				state.setHeight(sizeHeight);
			}

			double estimatedSize = sizeWidth + (cells.length - 1) * 70 + 50;
			if (estimatedSize > maxLaneWidth) {
				maxLaneWidth = estimatedSize;
			}

			if (!other) {
				maxLaneWidth = maxLaneWidth + 30;
			}

			geo.setWidth(maxLaneWidth);
			state.setWidth(maxLaneWidth);

			model.setGeometry(laneIt, geo);

		}

	}

	public void arrangeSwimlanePosition(final GraphComponent graphComponent) {
		final Graph graph = graphComponent.getGraph();
		Collection<Object> pools = mxGraphModel.filterDescendants(graph.getModel(), new mxGraphModel.Filter() {
			@Override
			public boolean filter(Object cell) {
				return graph.isAutoPool(cell);
			}
		});

		List<Object> poolList = new ArrayList<Object>();
		poolList.addAll(pools);
		Collections.sort(poolList, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				mxGeometry geo1 = ((mxCell) o1).getGeometry();
				mxGeometry geo2 = ((mxCell) o2).getGeometry();
				if (graph.isVerticalSwimlane(o1)) {
					return (int) (geo1.getX() - geo2.getX());
				} else {
					return (int) (geo1.getY() - geo2.getY());
				}
			}

		});

		for (Object pool : poolList) {
			arrangePoolYOffset(graphComponent, pool);
			arrangeLaneYOffset(graph, pool);
		}
	}

	public static void arrangePoolYOffset(final GraphComponent graphComponent, Object cell) {
		final Graph graph = graphComponent.getGraph();
		if (graph.isManualPool(cell)) {
			return;
		}
		GraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(cell);
		mxCellState state = graph.getView().getState(cell);
		Rectangle rect = geo.getRectangle();
		double yOffset = Constants.POOL_SPACING;
		Object pool = graphComponent.getCellAt(Constants.SWIMLANE_WIDTH / 4, rect.y - Constants.POOL_SPACING / 2);
		if (pool == null) {
			pool = graphComponent.getCellAt(Constants.SWIMLANE_WIDTH / 4, rect.y - 1);
		}
		if (pool == null) {
			pool = graphComponent.getCellAt(Constants.SWIMLANE_WIDTH / 4, rect.y - Constants.POOL_SPACING);
		}
		if (model.isLane(pool)) {
			pool = model.getParentPool(pool);
		}
		if (pool != cell && model.isPool(pool)) {
			mxGeometry tmp = model.getGeometry(pool);
			yOffset += tmp.getY() + tmp.getHeight();
			geo.setY(yOffset);
			model.setGeometry(cell, geo);
		} else if (pool == null && model.isPool(cell)) {
			mxGeometry tmp = model.getGeometry(cell);
			if (tmp.getY() < 50) {
				geo.setY(Constants.SWIMLANE_START_POINT);
			}
			model.setGeometry(cell, geo);
		}

		state.setX(geo.getX());
		state.setY(geo.getY());
		state.setWidth(geo.getWidth());
		state.setHeight(geo.getHeight());

	}

	public static void arrangeLaneYOffset(final Graph graph, Object cell) {
		if (graph.hasChildLane(cell)) {
			GraphModel model = graph.getModel();
			double yOffset = 0;
			int laneCount = model.getChildCount(cell);

			for (int i = 0; i < laneCount; i++) {
				Object lane = model.getChildAt(cell, i);
				if (model.isLane(lane)) {
					mxGeometry geo = model.getGeometry(lane);
					mxCellState state = graph.getView().getState(lane);
					geo.setY(yOffset);
					geo.setX(geo.getX() - 20);
					model.setGeometry(lane, geo);
					state.setY(geo.getY());
					state.setX(geo.getX());
					yOffset += model.getGeometry(lane).getHeight();
					arrangeLaneYOffset(graph, lane);
				}
			}
		}
	}

}
