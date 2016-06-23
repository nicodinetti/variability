package uy.edu.fing.modeler.variability.ui;

import java.util.Map;
import java.util.Properties;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.wizard.Wizard;

import uy.edu.fing.modeler.variability.ui.model.ModelVariant;

@SuppressWarnings("restriction")
public class MyWizard extends Wizard {

	protected MyPageOne one;
	protected MyPageTwo two;
	protected MyPageThree three;
	protected ModelVariant variant;
	private File file;
	private Map<String, Properties> configs;

	public MyWizard(File file, ModelVariant variant, Map<String, Properties> configs) {
		super();
		this.file = file;
		this.variant = variant;
		this.configs = configs;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return "Generando variante a partir de un proceso base";
	}

	@Override
	public void addPages() {
		one = new MyPageOne();
		two = new MyPageTwo();
		three = new MyPageThree();
		addPage(one);
		addPage(two);
		addPage(three);
	}

	public MyPageOne getOne() {
		return one;
	}

	public File getFile() {
		return file;
	}

	public Map<String, Properties> getConfigs() {
		return configs;
	}

	public ModelVariant getVariant() {
		return variant;
	}

	public String getBasePath() {
		return file.getParent().getRawLocation().toString();
	}

	public String getBaseProcessFileName() {
		return file.getName();
	}

	public Map<String, String> getSelectedVariants() {
		return one.getComboSelecteds();
	}

	public String getConfigName() {
		return one.getConfigName();
	}

	public String getResultFileName() {
		return one.getResultFileName();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
