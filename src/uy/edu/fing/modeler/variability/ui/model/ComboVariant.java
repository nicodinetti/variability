package uy.edu.fing.modeler.variability.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ComboVariant extends Combo {

	private static final String ELEGIR = "-- Elegir --";

	private String varName;
	private List<ComboVariant> comboVariants = new ArrayList<>();
	private Label label;

	public ComboVariant(Composite parent, int style, Label label) {
		super(parent, style);
		this.label = label;
		add(ELEGIR);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public List<ComboVariant> getComboVariants() {
		return comboVariants;
	}

	public void setComboVariants(List<ComboVariant> comboVariants) {
		this.comboVariants = comboVariants;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		label.setVisible(visible);
		if (!visible) {
			comboVariants.stream().forEach(x -> x.setVisible(false));
		}
	}

	public void clean() {
		super.select(0);
		comboVariants.stream().forEach(x -> x.clean());
	}

	@Override
	public String toString() {
		return "ComboVariant [varName=" + varName + ", comboVariants=" + comboVariants + "]";
	}

	public boolean selectVariant(String key, String value) {
		return selectVariantImpl(comboVariants, key, value);
	}

	private boolean selectVariantImpl(List<ComboVariant> comboVariants, String key, String value) {

		if (comboVariants.stream().anyMatch(x -> x.getVarName().equals(key))) {
			this.setText(value);
			return true;
		}

		for (ComboVariant comboVariant : comboVariants) {
			if (selectVariantImpl(comboVariant.getComboVariants(), key, value)) {
				return true;
			}
		}

		return false;
	}

	public boolean isAllSelected() {

		boolean res = false;
		if (!this.isVisible() || !getText().equals(ELEGIR)) {
			res = true;
		}

		for (ComboVariant comboVariant : comboVariants) {
			res = res || comboVariant.isAllSelected();
		}

		return res;
	}
}
