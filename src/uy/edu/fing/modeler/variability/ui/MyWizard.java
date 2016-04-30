package uy.edu.fing.modeler.variability.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.wizard.Wizard;

@SuppressWarnings("restriction")
public class MyWizard extends Wizard {

	protected MyPageOne one;
	protected MyPageTwo two;
	protected Map<String, List<String>> files;
	private File file;

	public MyWizard(File file, Map<String, List<String>> files) {
		super();
		this.file = file;
		this.files = files;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return "Select variabilities";
	}

	@Override
	public void addPages() {
		one = new MyPageOne(files);
		two = new MyPageTwo();
		addPage(one);
		addPage(two);
	}

	@Override
	public boolean performFinish() {
		System.out.println(one.getConfigName());

		return true;
	}

	public MyPageOne getOne() {
		return one;
	}

	public File getFile() {
		return file;
	}

}
