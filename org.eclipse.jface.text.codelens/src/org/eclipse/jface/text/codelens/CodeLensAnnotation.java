package org.eclipse.jface.text.codelens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;

public class CodeLensAnnotation extends Annotation {
	/**
	 * The type of codelens annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.codelens"; //$NON-NLS-1$

	private final List<ICodeLens> lenses;

	public CodeLensAnnotation() {
		super(TYPE, false, "");
		this.lenses = new ArrayList<>();
	}

	public void addCodeLens(ICodeLens codeLens) {
		lenses.clear();
		lenses.add(codeLens);
	}

	public List<ICodeLens> getLenses() {
		return lenses;
	}

}
