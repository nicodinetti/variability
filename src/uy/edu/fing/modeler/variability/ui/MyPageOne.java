package uy.edu.fing.modeler.variability.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class MyPageOne extends WizardPage {
	private Text configName;
	private Composite container;
	private Map<String, List<String>> files;
	private Map<String, Combo> selecteds = new HashMap<>();

	public MyPageOne() {
		super("Configuración");
		setTitle("Configuration y selección de variantes");
		setDescription("Seleccione las variantes a utilizar para cada tipo de punto de variación del proceso base seleccionado.");
	}

	public MyPageOne(Map<String, List<String>> files) {
		super("Configuración");
		setTitle("Configuration y selección de variantes");
		setDescription("Seleccione las variantes a utilizar para cada tipo de punto de variación del proceso base seleccionado.");
		this.files = files;
	}

	@Override
	public void createControl(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, NONE);
		container.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		Label lConfigName = new Label(container, NONE);
		lConfigName.setText("Nombre de la configuración: ");

		configName = new Text(container, SWT.BORDER | SWT.SINGLE);
		configName.setLayoutData(gd);
		configName.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (allSelected()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}

		});

		for (String vpOption : files.keySet()) {
			Label vpName = new Label(container, NONE);
			vpName.setText(vpOption + ": ");

			Combo options = new Combo(container, NONE);
			options.setItems(files.get(vpOption).toArray(new String[0]));
			options.add("DELETE");
			options.setLayoutData(gd);
			options.select(0);

			selecteds.put(vpOption, options);
		}

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("Estoy p1!");
			}
		});

	}

	private boolean allSelected() {
		boolean allComboSeleteds = selecteds.values().stream().allMatch(x -> x.getText() != null);
		if (!configName.getText().isEmpty() && allComboSeleteds) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return allSelected();
	}

	public String getConfigName() {
		return configName.getText();
	}

	public Map<String, String> getComboSelecteds() {
		Map<String, String> selectedVariants = new HashMap<>();
		for (String key : selecteds.keySet()) {
			selectedVariants.put(key, selecteds.get(key).getText());
		}
		return selectedVariants;
	}

}
