package uy.edu.fing.modeler.variability.ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

@SuppressWarnings("restriction")
public class MyPageThree extends WizardPage {

	private Composite container;
	private Label lresult;
	private Label lresultFile;
	private Label lresultFile2;

	public MyPageThree() {
		super("Finalización");
		setTitle("Guardado del resultado y la configuración.");
		setDescription("Guardado del resultado y la configuración.");
	}

	@Override
	public void createControl(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, NONE);
		container.setLayout(layout);

		lresult = new Label(container, NONE);
		new Label(container, NONE);
		lresultFile = new Label(container, NONE);
		new Label(container, NONE);
		lresultFile2 = new Label(container, NONE);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				try {
					String saveConfiguration = saveConfiguration();
					lresult.setText("Se han guardado los archivos de la variante en:");
					lresultFile.setText(saveConfiguration.replace(".conf", ".bpmn"));
					lresultFile2.setText(saveConfiguration);
					setPageComplete(true);

				} catch (IOException e) {
					e.printStackTrace();
					lresult.setText("Error al persistir los archivos resultado de la variante.");
					setPageComplete(false);
				}

				container.pack();
			}
		});

	}

	public String saveConfiguration() throws IOException {
		MyWizard myWizard = (MyWizard) getWizard();
		String basePath = myWizard.getFile().getParent().getRawLocation().toString();
		Path filepathResult = Paths.get(basePath + java.io.File.separatorChar + myWizard.getConfigName() + ".conf");

		Files.deleteIfExists(filepathResult);
		Files.createFile(filepathResult);

		try (BufferedWriter writer = Files.newBufferedWriter(filepathResult, Charset.defaultCharset())) {
			for (String key : myWizard.getSelectedVariants().keySet()) {
				writer.append(key + "=" + myWizard.getSelectedVariants().get(key) + "\n");
			}
		} catch (IOException e) {
			throw e;
		}

		return filepathResult.toString();
	}

}
