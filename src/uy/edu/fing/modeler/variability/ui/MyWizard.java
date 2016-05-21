package uy.edu.fing.modeler.variability.ui;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.wizard.Wizard;

@SuppressWarnings("restriction")
public class MyWizard extends Wizard {

	protected MyPageOne one;
	protected MyPageTwo two;
	protected MyPageThree three;
	protected Map<String, List<String>> files;
	private File file;
	private Map<String, Properties> configs;

	public MyWizard(File file, Map<String, List<String>> files, Map<String, Properties> configs) {
		super();
		this.file = file;
		this.files = files;
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

	public Map<String, List<String>> getFiles() {
		return files;
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
