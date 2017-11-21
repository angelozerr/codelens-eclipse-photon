package org.eclipse.jface.text.source.inlined;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

class InlinedAnnotationUtilities {

	/**
	 * Returns the line position by taking care of leading spaces.
	 * 
	 * @param lineIndex
	 *            the line index
	 * @param document
	 *            the document
	 * @return the line position by taking care of leading spaces.
	 * @throws BadLocationException
	 */
	public static Position getPosition(int lineIndex, IDocument document, boolean leadingSpaces)
			throws BadLocationException {
		int offset = document.getLineOffset(lineIndex);
		int lineLength = document.getLineLength(lineIndex);
		String line = document.get(offset, lineLength);
		if (leadingSpaces) {
			offset += getLeadingSpaces(line);
		}
		return new Position(offset, 1);
	}

	/**
	 * Returns the leading spaces of the given line text.
	 * 
	 * @param line
	 *            the line text.
	 * @return the leading spaces of the given line text.
	 */
	public static int getLeadingSpaces(String line) {
		int counter = 0;
		char[] chars = line.toCharArray();
		for (char c : chars) {
			if (c == '\t')
				counter++;
			else if (c == ' ')
				counter++;
			else
				break;
		}
		return counter;
	}

}