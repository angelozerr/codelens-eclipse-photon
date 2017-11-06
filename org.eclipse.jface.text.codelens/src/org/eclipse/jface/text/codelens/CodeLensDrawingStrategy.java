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
		if (!(annotation instanceof CodeLensAnnotation)) {
			return;
		}
		CodeLensAnnotation ann = (CodeLensAnnotation) annotation;
		int lineIndex = textWidget.getLineAtOffset(offset);
		int lineOffset = textWidget.getOffsetAtLine(lineIndex);
		// int nextOffset = textWidget.getOffsetAtLine(lineIndex + 1) - 1;
		length = 1;
		if (gc != null) {

			if (length < 1)
				return;

			for (ICodeLens codeLens : ann.getLenses()) {

				Point left = textWidget.getLocationAtOffset(lineOffset);
				gc.setForeground(textWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
				gc.drawText(codeLens.getCommand().getTitle(), left.x, left.y + 20);
			}

		} else {
			textWidget.redrawRange(lineOffset, length, true);
		}
	}

}
