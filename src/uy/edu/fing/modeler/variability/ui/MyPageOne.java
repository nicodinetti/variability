package uy.edu.fing.modeler.variability.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import uy.edu.fing.modeler.variability.ui.model.ComboVariant;
import uy.edu.fing.modeler.variability.ui.model.ModelVariant;

@SuppressWarnings("restriction")
public class MyPageOne extends WizardPage {

	private static final String DELETE = "DELETE";

	private Label lNewConfigName;
	private Text newConfigName;
	private Composite container;
	private List<ComboVariant> comboVariants;
	private boolean newConfig;
	private ComboVariant configOptions;

	public MyPageOne() {
		super("Configuración");
		setTitle("Configuration y selección de variantes");
		setDescription("Seleccione las variantes a utilizar para cada tipo de punto de variación del proceso base seleccionado.");
	}

	@Override
	public void createControl(Composite parent) {

		MyWizard myWizard = (MyWizard) getWizard();

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, SWT.NONE);
		container.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		Label lBaseProcess = new Label(container, SWT.NONE);
		lBaseProcess.setText("Proceso base: ");

		Label baseProcess = new Label(container, SWT.NONE);
		baseProcess.setLayoutData(gd);
		baseProcess.setText(myWizard.getFile().getName());

		// Espacios
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lConfigName = new Label(container, SWT.NONE);
		lConfigName.setText("Configuración: ");

		configOptions = new ComboVariant(container, SWT.DEFAULT, lConfigName);
		myWizard.getConfigs().keySet().stream().forEach(x -> configOptions.add(x));
		configOptions.add("Nueva configuración...");
		configOptions.setLayoutData(gd);
		configOptions.select(0);
		configOptions.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cleanSelection();

				Combo configOptions = (Combo) arg0.getSource();
				String selectedConfig = configOptions.getText();
				newConfig = selectedConfig.equals("Nueva configuración...");
				newConfigName.setVisible(newConfig);
				lNewConfigName.setVisible(newConfig);
				setPageComplete(allSelected());

				if (!newConfig) {
					// Seleccionar las opciones de la configuración elegida

					// Me tengo que quedar solo con el nombre
					newConfigName.setText(selectedConfig.replace(".conf", ""));

					Properties configFile = myWizard.getConfigs().get(selectedConfig);
					if (configFile != null) {
						for (Object value : configFile.keySet()) {
							String valueString = value.toString();

							boolean anyMatch = comboVariants.stream().anyMatch(x -> x.selectVariant(valueString, configFile.getProperty(valueString)));
							if (!anyMatch) {
								VariabilityPlugIn.failMessage(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Archivo de configuración inválido");
								cleanSelection();
								break;
							}
						}
					}
				} else {
					cleanSelection();
				}

				setPageComplete(allSelected());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		lNewConfigName = new Label(container, SWT.NONE);
		lNewConfigName.setText("Nombre de la configuración: ");
		lNewConfigName.setVisible(false);

		newConfigName = new Text(container, SWT.NONE);
		newConfigName.setVisible(false);
		newConfigName.setLayoutData(gd);
		newConfigName.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				setPageComplete(allSelected());
			}

		});

		Label lSelections = new Label(container, SWT.NONE);
		lSelections.setText("Selecciones: ");
		new Label(container, SWT.NONE);

		ModelVariant variant = myWizard.getVariant();
		comboVariants = addOptions(gd, variant, true);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event e) {
			}
		});

	}

	private List<ComboVariant> addOptions(GridData gd, ModelVariant variant, boolean visible) {

		List<ComboVariant> res = new ArrayList<>();

		for (ModelVariant varPoint : variant.getModelVariants()) {

			String varName = varPoint.getVarpointName();

			Label label = new Label(container, SWT.NONE);
			label.setText(variant.getFileName() + "/" + varName + ": ");
			label.setVisible(visible);

			ComboVariant resCombo = new ComboVariant(container, SWT.DEFAULT, label);
			resCombo.setVarName(variant.getFileName() + "/" + varName);
			varPoint.getModelVariants().stream().forEach(x -> {
				if (x.getVarpointName().endsWith(".bpmn")) {
					resCombo.add(x.getVarpointName());
				} else {
					resCombo.add(x.getVarpointName() + File.separatorChar + x.getVarpointName() + ".bpmn");
				}
			});
			resCombo.add(DELETE);
			resCombo.setLayoutData(gd);
			resCombo.select(0);
			resCombo.setVisible(visible);
			resCombo.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {

					ComboVariant source = (ComboVariant) arg0.getSource();

					source.getComboVariants().stream().forEach(x -> {
						System.out.println(source.getText() + " - " + x.getVarName());
						if (x.getVarName().contains(source.getText())) {
							x.setVisible(true);
						} else {
							x.setVisible(false);
						}
					});

					setPageComplete(allSelected());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});

			for (ModelVariant variantRec : varPoint.getModelVariants()) {
				List<ComboVariant> cvs = addOptions(gd, variantRec, false);
				resCombo.getComboVariants().addAll(cvs);
			}

			res.add(resCombo);
		}

		return res;
	}

	private boolean allSelected() {
		boolean allComboSeleteds = comboVariants.stream().allMatch(x -> x.isAllSelected());
		if (allComboSeleteds && configOptions.isAllSelected() && !newConfigName.getText().isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return allSelected();
	}

	public String getConfigName() {
		return newConfigName.getText();
	}

	public String getResultFileName() {
		return newConfigName.getText() + ".bpmn";
	}

	public Map<String, String> getComboSelecteds() {
		Map<String, String> res = new HashMap<>();
		List<Map<String, String>> collect = comboVariants.stream().map(x -> x.getComboSelecteds()).collect(Collectors.toList());
		for (Map<String, String> map : collect) {
			res.putAll(map);
		}
		return res;
	}

	public void cleanSelection() {
		newConfigName.setText("");
		comboVariants.stream().forEach(x -> x.clean());
	}

}
