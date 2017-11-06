package org.eclipse.ui.texteditor.codelens;

import org.eclipse.jface.text.codelens.AbstractCodeLens;
import org.eclipse.jface.text.codelens.ICodeLensProvider;

public class ClassCodeLens extends AbstractCodeLens {

	private final String className;

	public ClassCodeLens(String className, int afterLineNumber, ICodeLensProvider provider) {
		super(afterLineNumber, provider);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
