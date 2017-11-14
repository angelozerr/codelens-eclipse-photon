/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeLens support - Bug XXXXXX
 */
package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * Abstract class for {@link ICodeLens}.
 *
 */
public abstract class AbstractCodeLens implements ICodeLens {

	/**
	 * 
	 */
	private final Position position;

	/**
	 * 
	 */
	private final ICodeLensResolver resolver;

	/**
	 * 
	 */
	private Command command;

	public AbstractCodeLens(int beforeLineNumber, IDocument document) throws BadLocationException {
		this(beforeLineNumber, document, null);
	}

	public AbstractCodeLens(int beforeLineNumber, IDocument document, ICodeLensResolver provider)
			throws BadLocationException {
		this.position = CodeLensHelper.getPosition(beforeLineNumber, document);
		this.resolver = provider;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	@Override
	public ICodeLensResolver getResolver() {
		return resolver;
	}

	@Override
	public boolean isResolved() {
		return resolver == null || command != null;
	}

}
