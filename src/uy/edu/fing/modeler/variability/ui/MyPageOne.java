package uy.edu.fing.modeler.variability.ui;

import java.util.HashMap;
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

@SuppressWarnings("restriction")
public class MyPageOne extends WizardPage {

	private static final String ELEGIR = "-- Elegir --";

	private Label lNewConfigName;
	private Text newConfigName;
	private Composite container;
	private Map<String, Combo> comboSelecteds = new HashMap<>();
	private boolean newConfig;
	private Combo configOptions;

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

		configOptions = new Combo(container, SWT.DEFAULT);
		configOptions.add(ELEGIR);
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
					for (Object value : configFile.keySet()) {
						String valueString = value.toString();
						Combo combo = comboSelecteds.get(valueString);
						if (combo == null) {
							VariabilityPlugIn.failMessage(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Archivo de configuración inválido");
							cleanSelection();
							break;
						}
						combo.setText(configFile.getProperty(valueString));
						setPageComplete(true);
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

		// Espacios
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lSelections = new Label(container, SWT.NONE);
		lSelections.setText("Selecciones: ");
		new Label(container, SWT.NONE);

		for (String vpOption : myWizard.getFiles().keySet()) {
			Label vpName = new Label(container, SWT.NONE);
			vpName.setText(vpOption + ": ");

			Combo options = new Combo(container, SWT.DEFAULT);
			options.add(ELEGIR);
			myWizard.getFiles().get(vpOption).stream().forEach(x -> options.add(x));
			options.add("DELETE");
			options.setLayoutData(gd);
			options.select(0);
			options.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					setPageComplete(allSelected());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});

			comboSelecteds.put(vpOption, options);
		}

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event e) {
			}
		});

	}

	private boolean allSelected() {
		boolean allComboSeleteds = comboSelecteds.values().stream().allMatch(x -> !x.getText().equals(ELEGIR));
		if (!configOptions.getText().equals(ELEGIR) && !newConfigName.getText().isEmpty() && allComboSeleteds) {
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
		Map<String, String> collect = comboSelecteds.keySet().stream().collect(Collectors.toMap(x -> x, x -> comboSelecteds.get(x).getText()));
		return collect;
	}

	public void cleanSelection() {
		newConfigName.setText("");
		comboSelecteds.keySet().stream().forEach(x -> comboSelecteds.get(x).select(0));
	}

}
