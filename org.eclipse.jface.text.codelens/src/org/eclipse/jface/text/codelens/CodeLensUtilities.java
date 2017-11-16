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

import java.util.Iterator;
import java.util.function.Consumer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

/**
 * Provides methods for codelens.
 * <p>
 * This class is neither intended to be instantiated nor subclassed.
 * </p>
 *
 * @since 3.107
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
class CodeLensUtilities {

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

	/**
	 * Execute UI {@link StyledText} function which requires UI Thread.
	 * 
	 * @param text
	 *            the styled text
	 * @param fn
	 *            the function to execute.
	 */
	public static void runInUIThread(StyledText text, Consumer<StyledText> fn) {
		if (text == null || text.isDisposed()) {
			return;
		}
		Display display = text.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			fn.accept(text);
		} else {
			display.asyncExec(() -> {
				if (text.isDisposed()) {
					return;
				}
				fn.accept(text);
			});
		}
	}

	/**
	 * Returns the {@link CodeLensAnnotation} from the given line index and null
	 * otherwise.
	 * 
	 * @param viewer
	 *            the source viewer
	 * @param lineIndex
	 *            the line index.
	 * @return the {@link CodeLensAnnotation} from the given line index and null
	 *         otherwise.
	 */
	public static CodeLensAnnotation getCodeLensAnnotationAtLine(ISourceViewer viewer, int lineIndex) {
		if (viewer == null) {
			return null;
		}
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null) {
			return null;
		}
		IDocument document = viewer.getDocument();
		int lineNumber = lineIndex + 1;
		if (lineNumber > document.getNumberOfLines()) {
			return null;
		}
		try {
			IRegion line = document.getLineInformation(lineNumber);
			Iterator<Annotation> iter = (annotationModel instanceof IAnnotationModelExtension2)
					? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(line.getOffset(),
							line.getLength(), true, true)
					: annotationModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation ann = iter.next();
				if (ann instanceof CodeLensAnnotation) {
					Position p = annotationModel.getPosition(ann);
					if (p != null) {
						if (p.overlapsWith(line.getOffset(), line.getLength())) {
							return (CodeLensAnnotation) ann;
						}
					}
				}
			}
		} catch (BadLocationException e) {
		}
		return null;
	}
}
