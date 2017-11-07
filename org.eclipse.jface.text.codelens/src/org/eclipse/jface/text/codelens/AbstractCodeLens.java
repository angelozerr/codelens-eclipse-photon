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

	private final Position position;
	private final ICodeLensProvider provider;
	private Command command;

	public AbstractCodeLens(int afterLineNumber, IDocument document, ICodeLensProvider provider)
			throws BadLocationException {
		this.position = create(afterLineNumber, document);
		this.provider = provider;
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
	public ICodeLensProvider getProvider() {
		return provider;
	}

	private static Position create(int afterLineNumber, IDocument document) throws BadLocationException {
		int offset = document.getLineOffset(afterLineNumber - 1);
		return new Position(offset, 1);
	}

}
