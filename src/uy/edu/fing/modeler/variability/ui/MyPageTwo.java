package uy.edu.fing.modeler.variability.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uy.edu.fing.modeler.variability.core.ReemplazadorMain;

public class MyPageTwo extends WizardPage {
	private Composite container;

	public MyPageTwo() {
		super("Result");
		setTitle("Result");
		setDescription("Result");
	}

	@Override
	public void createControl(Composite parent) {

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		container = new Composite(parent, NONE);
		container.setLayout(layout);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		container.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event e) {
				MyWizard myWizard = (MyWizard) getWizard();
				MyPageOne pageOne = myWizard.getOne();
				Map<String, Combo> selecteds = pageOne.getSelecteds();
				String configName = pageOne.getConfigName();
				File file = myWizard.getFile();

				String basePath = file.getParent().getRawLocation().toString();
				String baseProcessFileName = file.getName();
				Map<String, String> selectedVariants = new HashMap<>();
				for (String key : selecteds.keySet()) {
					selectedVariants.put(key, selecteds.get(key).getText());
				}
				String resultFileName = "result.bpmn";
				ReemplazadorMain.replace(basePath, baseProcessFileName, selectedVariants, resultFileName);

				GridData gd = new GridData(GridData.FILL_HORIZONTAL);

				Label lConfigName = new Label(container, NONE);
				lConfigName.setText("Configuration name: ");

				Label configSelection = new Label(container, NONE);
				configSelection.setText(configName + ".conf");
				configSelection.setLayoutData(gd);

			}
		});

	}

}
