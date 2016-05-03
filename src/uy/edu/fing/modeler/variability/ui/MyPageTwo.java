package uy.edu.fing.modeler.variability.ui;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uy.edu.fing.modeler.variability.core.ReemplazadorMain;

public class MyPageTwo extends WizardPage {

	Composite container;
	private Label lConfigName;
	private Label configSelection;

	private Label lbutton;
	private Button button;

	private Label lresult;
	private Label lresultFile;
	private Label error;

	private boolean finish;

	public MyPageTwo() {
		super("Ejecutor");
		setTitle("Ejecutar generación");
		setDescription("Se ejecutará la generación de la variante en base a la selección anterior, y se persistirá la configuración para futuras generaciones de forma más sencilla");
	}

	@Override
	public void createControl(Composite parent) {

		// Datos de la página 1
		MyWizard myWizard = (MyWizard) getWizard();
		// Datos de la página 1

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, NONE);
		container.setLayout(layout);

		lConfigName = new Label(container, NONE);
		lConfigName.setText("Nombre de la configuración: ");
		configSelection = new Label(container, NONE);

		lbutton = new Label(container, NONE);
		lbutton.setText("Ejecutar geenración:");
		button = new Button(container, SWT.PUSH);
		button.setText("Generar!");

		lresult = new Label(container, NONE);
		lresultFile = new Label(container, NONE);
		error = new Label(container, NONE);

		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				String resultFileName = "result.bpmn";

				try {

					String basePath = myWizard.getBasePath();
					String baseProcessFileName = myWizard.getBaseProcessFileName();
					Map<String, String> selectedVariants = myWizard.getSelectedVariants();

					ReemplazadorMain.replace(basePath, baseProcessFileName, selectedVariants, resultFileName);
					lresult.setText("Se generó la variante correctamente: ");
					lresultFile.setText(basePath + java.io.File.separatorChar + resultFileName);
					setPageComplete(true);

				} catch (Exception e1) {
					error.setText("Falló el proceso de generación: " + e1.getMessage());
				}

				container.pack();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				System.out.println("Estoy en p2!");
				String configName = myWizard.getConfigName();
				configSelection.setText(configName + ".conf");
				error.setText("");
				container.pack();
			}
		});

	}

}
