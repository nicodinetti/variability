package uy.edu.fing.modeler.variability.ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		return "Generando variante a partir de un proceso base";
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
		try {
			saveConfiguration();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public MyPageOne getOne() {
		return one;
	}

	public File getFile() {
		return file;
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

	public void saveConfiguration() throws IOException {
		file.getParent().getRawLocation().toString();
		Path filepathResult = Paths.get(getBasePath() + java.io.File.separatorChar + one.getConfigName() + ".conf");

		Files.createFile(filepathResult);

		try (BufferedWriter writer = Files.newBufferedWriter(filepathResult, Charset.defaultCharset())) {
			for (String key : getSelectedVariants().keySet()) {
				writer.append(key + "=" + getSelectedVariants().get(key));
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public String getConfigName() {
		return one.getConfigName();
	}

}
