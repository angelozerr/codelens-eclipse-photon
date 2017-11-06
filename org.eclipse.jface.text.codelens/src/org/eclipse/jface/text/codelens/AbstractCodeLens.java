package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

public abstract class AbstractCodeLens implements ICodeLens {

	private final Position position;
	private final ICodeLensProvider provider;
	private Command command;
	
	public AbstractCodeLens(int afterLineNumber, IDocument document, ICodeLensProvider provider) throws BadLocationException {
		this.position = create(afterLineNumber, document);
		this.provider = provider;
	}

	@Override
	public Position getPosition()  {
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
		//IRegion lineInfo = document.getLineInformation(afterLineNumber - 1);
		//String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		int offset = document.getLineOffset(afterLineNumber - 1);
		return new Position(offset, 1);
	}

}
