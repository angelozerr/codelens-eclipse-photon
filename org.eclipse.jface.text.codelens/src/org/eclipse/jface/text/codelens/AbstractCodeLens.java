/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - CodeLens support - Bug 526969
 */
package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * Abstract class for {@link ICodeLens}.
 *
 * @since 3.107
 */
public abstract class AbstractCodeLens implements ICodeLens {

	/**
	 * The position where codelens must be drawn
	 */
	private final Position position;

	/**
	 * The resolver to resolve codelens if needed. If no need to resolve the
	 * resolver can be null.
	 */
	private final ICodeLensResolver resolver;

	/**
	 * The command of the resolved codelens.
	 */
	private Command command;

	/**
	 * Codelens constructor to locate the lens before the given line number.
	 * 
	 * @param beforeLineNumber
	 *            the line number where codelens must be drawn. Use 0 if you wish to
	 *            locate the lens before the first line number (1).
	 * @param document
	 *            the document.
	 * @throws BadLocationException
	 *             when line number doesn't exists
	 */
	public AbstractCodeLens(int beforeLineNumber, IDocument document) throws BadLocationException {
		this(beforeLineNumber, document, null);
	}

	/**
	 * Codelens constructor to locate the lens before the given line number.
	 * 
	 * @param beforeLineNumber
	 *            the line number where codelens must be drawn. Use 0 if you wish to
	 *            locate the lens before the first line number (1).
	 * @param document
	 *            the document.
	 * @param resolver
	 *            the codelens resolver
	 * @throws BadLocationException
	 *             when line number doesn't exists
	 */
	public AbstractCodeLens(int beforeLineNumber, IDocument document, ICodeLensResolver resolver)
			throws BadLocationException {
		this.position = CodeLensUtilities.getPosition(beforeLineNumber, document);
		this.resolver = resolver;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public Command getCommand() {
		return command;
	}

	/**
	 * Set the resolved command.
	 * 
	 * @param command
	 *            the resolved command.
	 */
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
