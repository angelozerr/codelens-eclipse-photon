/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.examples.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningResolver;

public class ClassCodeMining extends AbstractCodeMining {

	private final String className;

	public ClassCodeMining(String className, int afterLineNumber, IDocument document, ICodeMiningResolver resolver)
			throws BadLocationException {
		super(afterLineNumber, document, resolver);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
