/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide Java References/Implementation CodeMinings - Bug 529127
 */
package org.eclipse.jdt.junit.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Abstract class for Java code mining.
 *
 */
public abstract class AbstractJavaCodeMining extends AbstractCodeMining {

	private final IJavaElement element;

	public AbstractJavaCodeMining(IJavaElement element, IDocument document, ICodeMiningProvider provider) throws JavaModelException, BadLocationException {
		super(getLineNumber(element, document), document, provider);
		this.element= element;
	}

	private static int getLineNumber(IJavaElement element, IDocument document) throws JavaModelException, BadLocationException {
		ISourceRange r= ((ISourceReference) element).getNameRange();
		int offset= r.getOffset();
		return document.getLineOfOffset(offset);
	}

	/**
	 * Returns the java element.
	 * 
	 * @return the java element.
	 */
	public IJavaElement getElement() {
		return element;
	}

}
