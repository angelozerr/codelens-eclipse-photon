package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public abstract class AbstractCodeLens implements ICodeLens {

	private int afterLineNumber;
	private Command command;

	public AbstractCodeLens(int afterLineNumber) {
		this.afterLineNumber = afterLineNumber;
	}

	@Override
	public Position getPosition(IDocument document) throws BadLocationException {
		return new Position(document.getLineOffset(afterLineNumber - 1));
	}

	@Override
	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

}
