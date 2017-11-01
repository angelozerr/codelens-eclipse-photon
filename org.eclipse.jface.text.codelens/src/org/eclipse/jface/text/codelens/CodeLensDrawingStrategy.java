package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class CodeLensDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		System.err.println(annotation.getText());
		if (true) return;
		int lineIndex = textWidget.getLineAtOffset(offset);
		int lineOffset = textWidget.getOffsetAtLine(lineIndex);
		int nextOffset = textWidget.getOffsetAtLine(lineIndex + 1) - 1;
		length = 1;
		if (gc != null) {

   			if (length < 1)
				return;

			Point left = textWidget.getLocationAtOffset(lineOffset);
			gc.setForeground(textWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			gc.drawText(annotation.getText(), left.x, left.y + 20);

		} else {
			textWidget.redrawRange(nextOffset, length, true);
		}
	}

}
