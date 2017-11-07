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

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * {@link IDrawingStrategy} implementation to render {@link CodeLensAnnotation}
 * which are composed with list of {@link ICodeLens}.
 */
public class CodeLensDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof CodeLensAnnotation)) {
			return;
		}
		CodeLensAnnotation ann = (CodeLensAnnotation) annotation;
		int lineIndex = textWidget.getLineAtOffset(offset);
		if (gc != null) {
			// Loop for codelens and render it
			for (ICodeLens codeLens : ann.getLenses()) {
				Point left = textWidget.getLocationAtOffset(offset);
				gc.setForeground(textWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
				gc.drawText(codeLens.getCommand().getTitle(), left.x, left.y + 20);
			}
		} else {
			// Refresh the full line where CodeLens annotation must be drawn in the line
			// spacing
			int lineLength = getNextOffset(lineIndex, textWidget) - offset;
			textWidget.redrawRange(offset, lineLength, true);
		}
	}

	/**
	 * Returns the next offset of the given line idenx.
	 * 
	 * @param lineIndex
	 *            the line index
	 * @param textWidget
	 *            the text widget
	 * @return the next offset of the given line idenx.
	 */
	private int getNextOffset(int lineIndex, StyledText textWidget) {
		int nextLineIndex = lineIndex + 1;
		if (nextLineIndex >= textWidget.getLineCount()) {
			return textWidget.getCharCount();
		}
		return textWidget.getOffsetAtLine(nextLineIndex);
	}

}
