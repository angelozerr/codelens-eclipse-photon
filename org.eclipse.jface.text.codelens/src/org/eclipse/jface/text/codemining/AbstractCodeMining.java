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
package org.eclipse.jface.text.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * Abstract class for {@link ICodeMining}.
 *
 * @since 3.13.0
 */
public abstract class AbstractCodeMining implements ICodeMining {

	/**
	 * The position where codemining must be drawn
	 */
	private final Position position;

	/**
	 * The resolver to resolve codemining if needed. If no need to resolve the resolver can be null.
	 */
	private final ICodeMiningResolver resolver;

	/**
	 * The command of the resolved codemining.
	 */
	private Command command;

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 * 
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @throws BadLocationException when line number doesn't exists
	 */
	public AbstractCodeMining(int beforeLineNumber, IDocument document) throws BadLocationException {
		this(beforeLineNumber, document, null);
	}

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 * 
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @param resolver the codemining resolver
	 * @throws BadLocationException when line number doesn't exists
	 */
	public AbstractCodeMining(int beforeLineNumber, IDocument document, ICodeMiningResolver resolver)
			throws BadLocationException {
		this.position= CodeMiningUtilities.getPosition(beforeLineNumber, document);
		this.resolver= resolver;
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
	 * @param command the resolved command.
	 */
	public void setCommand(Command command) {
		this.command= command;
	}

	@Override
	public ICodeMiningResolver getResolver() {
		return resolver;
	}

	@Override
	public boolean isResolved() {
		return resolver == null || command != null;
	}

}
