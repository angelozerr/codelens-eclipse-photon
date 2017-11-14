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

class CodeLensHelper {

	/**
	 * Returns the line position by taking care of leading spaces.
	 * 
	 * @param lineIndex
	 *            line index
	 * @param document
	 *            the document
	 * @return the line position by taking care of leading spaces.
	 * @throws BadLocationException
	 */
	public static Position getPosition(int lineIndex, IDocument document) throws BadLocationException {
		int offset = document.getLineOffset(lineIndex);
		int lineLength = document.getLineLength(lineIndex);
		String line = document.get(offset, lineLength);
		offset += getLeadingSpaces(line);
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
