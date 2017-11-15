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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
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
		CodeLensDrawingStrategy.draw((CodeLensAnnotation) annotation, gc, textWidget, offset, length, color);
	}

	public static void draw(CodeLensAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, redraw the styled text to hide old draw of
			// annotations
			textWidget.redraw();
			// update caret offset since line spacing has changed.
			textWidget.setCaretOffset(textWidget.getCaretOffset());
			return;
		}
		int lineIndex = -1;
		try {
			lineIndex = textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int previousLineIndex = lineIndex - 1;
		if (previousLineIndex < 0) {
			return;
		}
		int previousOffset = textWidget.getOffsetAtLine(previousLineIndex);
		if (gc != null) {
			int lineOffset = textWidget.getOffsetAtLine(lineIndex)
					+ CodeLensHelper.getLeadingSpaces(textWidget.getLine(lineIndex));
			Point leftL = textWidget.getLocationAtOffset(lineOffset);

			Point left = textWidget.getLocationAtOffset(previousOffset);
			// Loop for codelens and render it
			String text = getText(new ArrayList<>(annotation.getLenses()), annotation.getText());
			gc.setForeground(color);
			gc.setBackground(textWidget.getBackground());
			Font font = annotation.getFont();
			if (font != null) {
				gc.setFont(font);
			}
			gc.drawText(text, leftL.x, left.y + annotation.getHeight());
		} else {
			// Refresh the full line where CodeLens annotation must be drawn in the line
			// spacing
			String text = getText(new ArrayList<>(annotation.getLenses()), null);
			if (text.length() == 0 || text.equals(annotation.getText())) {
				// CodeLens has not changed, no need to refresh it
				// FIXME: manage leading spaces (if leading spaces doens't changed, no need to
				// refresh it too)
				if (CodeLensHelper.getLeadingSpaces(textWidget.getLine(lineIndex)) == 0)
					return;
			}
			annotation.setText(text);

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
