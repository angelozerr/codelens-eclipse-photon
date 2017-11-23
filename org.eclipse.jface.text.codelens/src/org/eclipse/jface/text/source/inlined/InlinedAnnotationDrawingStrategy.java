package org.eclipse.jface.text.source.inlined;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class InlinedAnnotationDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof InlinedAnnotation)) {
			return;
		}
		InlinedAnnotationDrawingStrategy.draw((InlinedAnnotation) annotation, gc, textWidget, offset, length, color);
	}

	public static void draw(InlinedAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, redraw the styled text to hide old draw of
			// annotations
			textWidget.redraw();
			// update caret offset since line spacing has changed.
			textWidget.setCaretOffset(textWidget.getCaretOffset());
			return;
		}
		if (annotation.isShowAtBeforeLine()) {
			drawAtBeforeLine(annotation, gc, textWidget, offset, length, color);
		} else {
			drawInsideLine(annotation, gc, textWidget, offset, length, color);
		}
	}

	private static void drawAtBeforeLine(InlinedAnnotation annotation, GC gc, StyledText textWidget, int offset,
			int length, Color color) {
		int lineIndex = -1;
		try {
			lineIndex = textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			return;
		}
		int previousLineIndex = lineIndex - 1;
		if (gc != null) {
			int lineOffset = Positions.offset(lineIndex, textWidget, true);
			int x = textWidget.getLocationAtOffset(lineOffset).x;
			int y = 0;
			if (lineIndex > 0) {
				int previousOffset = textWidget.getOffsetAtLine(previousLineIndex);
				y = textWidget.getLocationAtOffset(previousOffset).y + annotation.getHeight(textWidget);
			}
			if (gc.getClipping().contains(x, y)) {
				annotation.draw(gc, textWidget, offset, length, color, x, y);
				return;
			} else {
				if (!annotation.isDirty()) {
					return;
				}
			}
		}

		if (previousLineIndex < 0) {
			return;
		}
		int previousOffset = textWidget.getOffsetAtLine(previousLineIndex);
		int lineLength = offset - previousOffset;
		textWidget.redrawRange(previousOffset, lineLength, true);

	}

	private static void drawInsideLine(InlinedAnnotation annotation, GC gc, StyledText textWidget, int offset,
			int length, Color color) {
		if (gc != null) {

			FontMetrics fontMetrics = gc.getFontMetrics();
			Rectangle bounds = textWidget.getTextBounds(offset, offset);
			int x = bounds.x + fontMetrics.getLeading();
			int y = bounds.y + fontMetrics.getDescent();

			annotation.draw(gc, textWidget, offset, length, color, x, y);

			// The square replaces the first character of the color by taking a place
			// (COLOR_SQUARE_WITH) by using GlyphMetrics
			// Here we need to redraw this first character because GlyphMetrics clip this
			// color character.
			String s = textWidget.getText(offset, offset);
			StyleRange style = textWidget.getStyleRangeAtOffset(offset);
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
