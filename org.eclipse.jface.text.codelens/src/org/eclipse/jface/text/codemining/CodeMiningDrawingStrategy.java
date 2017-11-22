/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;

/**
 * {@link IDrawingStrategy} implementation to render {@link CodeMiningAnnotation} composed with list
 * of {@link ICodeMining} for a given line.
 * 
 * @since 3.13.0
 */
public class CodeMiningDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof CodeMiningAnnotation)) {
			return;
		}
		CodeMiningDrawingStrategy.draw((CodeMiningAnnotation) annotation, gc, textWidget, offset, length, color);
	}

	public static void draw(CodeMiningAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, redraw the styled text to hide old draw of
			// annotations
			textWidget.redraw();
			// update caret offset since line spacing has changed.
			textWidget.setCaretOffset(textWidget.getCaretOffset());
			return;
		}
		int lineIndex= -1;
		try {
			lineIndex= textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			return;
		}

		if (gc != null) {
			int lineOffset= textWidget.getOffsetAtLine(lineIndex)
					+ CodeMiningUtilities.getLeadingSpaces(textWidget.getLine(lineIndex));
			Point leftL= textWidget.getLocationAtOffset(lineOffset);

			int y= 0;
			if (lineIndex > 0) {
				int previousLineIndex= lineIndex - 1;
				int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
				y= textWidget.getLocationAtOffset(previousOffset).y + annotation.getHeight();
			}

			// Loop for codemining and render it
			String text= getText(new ArrayList<>(annotation.getMininges()), annotation.getText());
			gc.setForeground(color);
			gc.setBackground(textWidget.getBackground());
			Font font= annotation.getFont();
			if (font != null) {
				gc.setFont(font);
			}
			gc.drawText(text, leftL.x, y);
		} else {
			int previousLineIndex= lineIndex - 1;
			if (previousLineIndex < 0) {
				return;
			}
			// Refresh the full line where CodeMining annotation must be drawn in the line
			// spacing
			String text= getText(new ArrayList<>(annotation.getMininges()), null);
			if (text.length() == 0 || text.equals(annotation.getText())) {
				// CodeMining has not changed, no need to refresh it
				// FIXME: manage leading spaces (if leading spaces doens't changed, no need to
				// refresh it too)
				if (CodeMiningUtilities.getLeadingSpaces(textWidget.getLine(lineIndex)) == 0)
					return;
			}
			annotation.setText(text);
			int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
			int lineLength= offset - previousOffset;
			textWidget.redrawRange(previousOffset, lineLength, true);
		}
	}

	private static String getText(List<ICodeMining> minings, String oldText) {
		StringBuilder text= new StringBuilder();
		for (ICodeMining codeMining : minings) {
			if (!codeMining.isResolved()) {
				// Don't render codemining which is not resolved.
				if (oldText != null) {
					return oldText;
				}
				continue;
			}
			if (text.length() > 0) {
				text.append(" | "); //$NON-NLS-1$
			}
			String title= codeMining.getCommand() != null ? codeMining.getCommand().getTitle() : "no command"; //$NON-NLS-1$
			text.append(title);
		}
		return text.toString();
	}

}
