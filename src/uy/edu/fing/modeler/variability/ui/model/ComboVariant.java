package uy.edu.fing.modeler.variability.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ComboVariant extends Combo implements SelectionListener {

	private static final String ELEGIR = "-- Elegir --";

	private String varName;
	private List<ComboVariant> comboVariants = new ArrayList<>();
	private Label label;

	public ComboVariant(Composite parent, int style, Label label) {
		super(parent, style);
		this.label = label;
		add(ELEGIR);
		addSelectionListener(this);
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

		if (varName.equals(key)) {
			this.setText(value);
			return true;
		}

		for (ComboVariant comboVariant : comboVariants) {
			if (comboVariant.selectVariant(key, value)) {
				return true;
			}
		}

		return false;
	}

	public boolean isAllSelected() {

		if (this.isVisible() && getText().equals(ELEGIR)) {
			return false;
		}

		boolean res = true;
		for (ComboVariant comboVariant : comboVariants) {
			res = res || comboVariant.isAllSelected();
		}

		return res;
	}

	public Map<String, String> getComboSelecteds() {
		Map<String, String> collect = new HashMap<>();

		if (!getText().equals(ELEGIR)) {

			collect.put(varName, getText());

			for (ComboVariant cv : comboVariants) {
				Map<String, String> comboSelectedsImpl = cv.getComboSelecteds();
				collect.putAll(comboSelectedsImpl);
			}

		}

		return collect;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		comboVariants.stream().forEach(x -> x.select(0));
	}
}
