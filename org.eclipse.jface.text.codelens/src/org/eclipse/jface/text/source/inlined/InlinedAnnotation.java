package org.eclipse.jface.text.source.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public abstract class InlinedAnnotation extends Annotation {

	/**
	 * The type of inlined annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.source.inlined"; //$NON-NLS-1$

	private final Position position;

	private final boolean showAtBeforeLine;

	public InlinedAnnotation(Position position, boolean showAtBeforeLine) {
		super(TYPE, false, ""); //$NON-NLS-1$
		this.position = position;
		this.showAtBeforeLine = showAtBeforeLine;
	}

	public Position getPosition() {
		return position;
	}

	public boolean isShowAtBeforeLine() {
		return showAtBeforeLine;
	}

	public abstract Integer getHeight();

	public abstract Integer getWidth();

	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		gc.setForeground(color);
		gc.setBackground(textWidget.getBackground());
		gc.drawText(getText(), x, y);
	}

}
