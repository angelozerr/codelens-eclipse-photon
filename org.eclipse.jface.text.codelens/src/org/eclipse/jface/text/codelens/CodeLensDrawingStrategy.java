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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * {@link IDrawingStrategy} implementation to render {@link CodeLensAnnotation}
 * composed with list of {@link ICodeLens} for a given line.
 * 
 * @since 3.107
 */
public class CodeLensDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof CodeLensAnnotation)) {
			return;
		}
		CodeLensDrawingStrategy.draw((CodeLensAnnotation) annotation, gc, textWidget, offset, length, color);
	}

	public static void draw(CodeLensAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// Annotation was deleted, ignore the draw
			return;
		}
		int lineIndex = -1;
		try {
			lineIndex = textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			return;
		}

		if (gc != null) {
			int lineOffset = textWidget.getOffsetAtLine(lineIndex)
					+ CodeLensUtilities.getLeadingSpaces(textWidget.getLine(lineIndex));
			Point leftL = textWidget.getLocationAtOffset(lineOffset);

			int y = 0;
			if (lineIndex > 0) {
				int previousLineIndex = lineIndex - 1;
				int previousOffset = textWidget.getOffsetAtLine(previousLineIndex);
				y = textWidget.getLocationAtOffset(previousOffset).y + annotation.getHeight();
			}
			// Loop for codelens and render it
			String text = getText(new ArrayList<>(annotation.getLenses()), annotation.getText());
			gc.setForeground(color);
			gc.setBackground(textWidget.getBackground());
			Font font = annotation.getFont();
			if (font != null) {
				gc.setFont(font);
			}
			gc.drawText(text, leftL.x, y);
		} else {
			int previousLineIndex = lineIndex - 1;
			if (previousLineIndex < 0) {
				return;
			}
			// Refresh the full line where CodeLens annotation must be drawn in the line
			// spacing
			String text = getText(new ArrayList<>(annotation.getLenses()), null);
			if (text.length() == 0 || text.equals(annotation.getText())) {
				// CodeLens has not changed, no need to refresh it
				// FIXME: manage leading spaces (if leading spaces doens't changed, no need to
				// refresh it too)
				if (CodeLensUtilities.getLeadingSpaces(textWidget.getLine(lineIndex)) == 0)
					return;
			}
			annotation.setText(text);
			int previousOffset = textWidget.getOffsetAtLine(previousLineIndex);
			int lineLength = offset - previousOffset;
			textWidget.redrawRange(previousOffset, lineLength, true);
		}
	}

	private static String getText(List<ICodeLens> lenses, String oldText) {
		StringBuilder text = new StringBuilder();
		for (ICodeLens codeLens : lenses) {
			if (!codeLens.isResolved()) {
				// Don't render codelens which is not resolved.
				if (oldText != null) {
					return oldText;
				}
				continue;
			}
			if (text.length() > 0) {
				text.append(" | ");
			}
			String title = codeLens.getCommand() != null ? codeLens.getCommand().getTitle() : "no command";
			text.append(title);
		}
		return text.toString();
	}

}
