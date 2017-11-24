/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;

/**
 * {@link IDrawingStrategy} implementation to render {@link AbstractInlinedAnnotation}.
 * 
 * @since 3.13.0
 */
public class InlinedAnnotationDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof AbstractInlinedAnnotation)) {
			return;
		}
		InlinedAnnotationDrawingStrategy.draw((AbstractInlinedAnnotation) annotation, gc, textWidget, offset, length, color);
	}

	/**
	 * Draw the inlined annotation.
	 * 
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	public static void draw(AbstractInlinedAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, redraw the styled text to hide old draw of
			// annotations
			textWidget.redraw();
			// update caret offset since line spacing has changed.
			textWidget.setCaretOffset(textWidget.getCaretOffset());
			return;
		}
		if (annotation instanceof LineHeaderAnnotation) {
			draw((LineHeaderAnnotation) annotation, gc, textWidget, offset, length, color);
		} else {
			draw((LineContentAnnotation) annotation, gc, textWidget, offset, length, color);
		}
	}

	/**
	 * Draw the line header annotation in the line spacing of the previous line.
	 * 
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private static void draw(LineHeaderAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		// compute current, previous line index.
		int lineIndex= -1;
		try {
			lineIndex= textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			return;
		}
		int previousLineIndex= lineIndex - 1;
		if (gc != null) {
			// Compute the location of the annotation
			int x= textWidget.getLocationAtOffset(offset).x;
			int y= 0;
			if (lineIndex > 0) {
				int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
				y= textWidget.getLocationAtOffset(previousOffset).y + annotation.getHeight();
			}
			if (gc.getClipping().contains(x, y)) {
				annotation.draw(gc, textWidget, offset, length, color, x, y);
				return;
			} else {
				if (!annotation.isDirty()) {
					//return;
				}
			}
		}

		if (previousLineIndex < 0) {
			// There are none previous line, do nothing
			return;
		}
		// refresh the previous line range where line header annotation must be drawn.
		int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
		int lineLength= offset - previousOffset;
		textWidget.redrawRange(previousOffset, lineLength, true);
	}

	/**
	 * Draw the line content annotation inside line in the empty area computed by {@link GlyphMetrics}.
	 * 
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private static void draw(LineContentAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (gc != null) {
			// Compute the location of the annotation
			FontMetrics fontMetrics= gc.getFontMetrics();
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			int x= bounds.x + fontMetrics.getLeading();
			int y= bounds.y + fontMetrics.getDescent();

			// Draw the line content annotation
			annotation.draw(gc, textWidget, offset, length, color, x, y);

			// The inline annotation replaces one character by taking a place width
			// GlyphMetrics
			// Here we need to redraw this first character because GlyphMetrics clip this
			// character.
			String s= textWidget.getText(offset, offset);
			StyleRange style= textWidget.getStyleRangeAtOffset(offset);
			if (style != null) {
				if (style.background != null) {
					gc.setBackground(style.background);
				}
				if (style.foreground != null) {
					gc.setForeground(style.foreground);
				}
			}
			gc.drawString(s, bounds.x + bounds.width - gc.stringExtent(s).x, bounds.y, true);
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}
}
