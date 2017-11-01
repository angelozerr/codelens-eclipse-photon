package org.eclipse.ui.texteditor.codelens;

import org.eclipse.jface.text.codelens.AbstractCodeLens;

public class ClassCodeLens extends AbstractCodeLens {

	private final String className;

	public ClassCodeLens(String className, int afterLineNumber) {
		super(afterLineNumber);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
