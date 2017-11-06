package org.eclipse.jface.text.codelens.samples;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codelens.AbstractCodeLens;
import org.eclipse.jface.text.codelens.ICodeLensProvider;

public class ClassCodeLens extends AbstractCodeLens {

	private final String className;

	public ClassCodeLens(String className, int afterLineNumber, IDocument document, ICodeLensProvider provider)
			throws BadLocationException {
		super(afterLineNumber, document, provider);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
