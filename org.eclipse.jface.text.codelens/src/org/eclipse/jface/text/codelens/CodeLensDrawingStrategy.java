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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * {@link IDrawingStrategy} implementation to render {@link CodeLensAnnotation}
 * composed with list of {@link ICodeLens} for a given line.
 */
public class CodeLensDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof CodeLensAnnotation)) {
			return;
		}
		CodeLensAnnotation ann = (CodeLensAnnotation) annotation;
		int lineIndex = textWidget.getLineAtOffset(offset);
		int nextLineIndex = lineIndex + 1;
		if (nextLineIndex >= textWidget.getLineCount()) {
			return;
		}
		int nextOffset = textWidget.getOffsetAtLine(nextLineIndex);
		if (gc != null) {
			// adjust offset with leading spaces of the next line
			nextOffset = nextOffset + getLeadingSpaces(textWidget.getLine(nextLineIndex));
			Point left = textWidget.getLocationAtOffset(nextOffset);
			// Loop for codelens and render it
			String text = getText(new ArrayList<>(ann.getLenses()));
			gc.setForeground(textWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			gc.drawText(text, left.x, left.y - ann.getHeight());
		} else {
			// Refresh the full line where CodeLens annotation must be drawn in the line
			// spacing
			int lineLength = nextOffset - offset;
			textWidget.redrawRange(offset, lineLength, true);
		}
	}

	private String getText(List<ICodeLens> lenses) {
		StringBuilder text = new StringBuilder();
		for (ICodeLens codeLens : lenses) {
			if (text.length() > 0) {
				text.append(" | ");
			}
			text.append(codeLens.getCommand().getTitle());
		}
		return text.toString();
	}

	private static int getLeadingSpaces(String line) {
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
