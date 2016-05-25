package uy.edu.fing.modeler.variability.ui;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uy.edu.fing.modeler.variability.core.ReemplazadorMain;

public class MyPageTwo extends WizardPage {

	private Composite container;
	private Label lConfigName;
	private Label configSelection;

	private Label result;

	public MyPageTwo() {
		super("Ejecutor");
		setTitle("Ejecutar generación");
		setDescription("Se ejecutará la generación de la variante en base a la selección anterior, y se persistirá la configuración para futuras generaciones de forma más sencilla");
	}

	@Override
	public void createControl(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, NONE);
		container.setLayout(layout);

		lConfigName = new Label(container, NONE);
		lConfigName.setText("Nombre de la configuración: ");
		configSelection = new Label(container, NONE);

		new Label(container, NONE);
		result = new Label(container, NONE);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				try {
					MyWizard myWizard = (MyWizard) getWizard();
					String configName = myWizard.getConfigName();
					String basePath = myWizard.getBasePath();
					String baseProcessFileName = myWizard.getBaseProcessFileName();
					String resultFileName = myWizard.getResultFileName();
					Map<String, String> selectedVariants = myWizard.getSelectedVariants();

					configSelection.setText(configName + ".conf");
					result.setText("");

					ReemplazadorMain.replace(basePath, baseProcessFileName, selectedVariants, resultFileName);
					result.setText("Se generó la variante correctamente");
					setPageComplete(true);

				} catch (Exception e) {
					result.setText("Falló el proceso de generación: " + e.getMessage());
					setPageComplete(false);
				}

				container.pack();
			}
		});

	}

}
