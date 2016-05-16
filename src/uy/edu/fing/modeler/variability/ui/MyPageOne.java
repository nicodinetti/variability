package uy.edu.fing.modeler.variability.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.internal.resources.File;
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

@SuppressWarnings("restriction")
public class MyPageOne extends WizardPage {
	private Label lNewConfigName;
	private Text newConfigName;
	private Composite container;
	private Map<String, List<String>> files;
	private Map<String, Combo> comboSelecteds = new HashMap<>();
	private Map<String, Combo> comboConfigs = new HashMap<>();
	private File file;
	private Map<String, Properties> configs;
	private boolean newConfig;
	private Combo configOptions;

	public MyPageOne() {
		super("Configuración");
		setTitle("Configuration y selección de variantes");
		setDescription("Seleccione las variantes a utilizar para cada tipo de punto de variación del proceso base seleccionado.");
	}

	public MyPageOne(File file, Map<String, List<String>> files, Map<String, Properties> configs) {
		super("Configuración");
		this.file = file;
		this.configs = configs;
		this.files = files;
		setTitle("Configuration y selección de variantes");
		setDescription("Seleccione las variantes a utilizar para cada tipo de punto de variación del proceso base seleccionado.");
	}

	@Override
	public void createControl(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, SWT.NONE);
		container.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		Label lBaseProcess = new Label(container, SWT.NONE);
		lBaseProcess.setText("Proceso base: ");

		Label baseProcess = new Label(container, SWT.NONE);
		baseProcess.setLayoutData(gd);
		baseProcess.setText(file.getName());

		// Espacios
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lConfigName = new Label(container, SWT.NONE);
		lConfigName.setText("Configuración: ");

		configOptions = new Combo(container, SWT.DEFAULT);
		configOptions.add("-- Elegir --");
		String[] array = configs.keySet().toArray(new String[0]);
		for (String value : array) {
			configOptions.add(value);
		}
		configOptions.add("Nueva configuración...");
		configOptions.setLayoutData(gd);
		configOptions.select(0);
		configOptions.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Combo configOptions = (Combo) arg0.getSource();
				newConfig = configOptions.getText().equals("Nueva configuración...");
				newConfigName.setVisible(newConfig);
				lNewConfigName.setVisible(newConfig);
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

		for (String vpOption : files.keySet()) {
			Label vpName = new Label(container, SWT.NONE);
			vpName.setText(vpOption + ": ");

			Combo options = new Combo(container, SWT.DEFAULT);
			options.setItems(files.get(vpOption).toArray(new String[0]));
			options.add("DELETE");
			options.setLayoutData(gd);
			// options.select(0);

			comboSelecteds.put(vpOption, options);
		}

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event e) {
				System.out.println("Estoy p1!");
			}
		});

	}

	private boolean allSelected() {
		boolean allComboSeleteds = comboSelecteds.values().stream().allMatch(x -> x.getText() != null);
		if (((!newConfig && !configOptions.getText().equals("-- Elegir --")) || (newConfig && !newConfigName.getText().isEmpty())) && allComboSeleteds) {
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
		Map<String, String> selectedVariants = new HashMap<>();
		for (String key : comboSelecteds.keySet()) {
			selectedVariants.put(key, comboSelecteds.get(key).getText());
		}
		return selectedVariants;
	}

}
