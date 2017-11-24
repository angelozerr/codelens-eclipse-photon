package org.eclipse.jface.text.source.inlined;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

/**
 * Abstract class for inlined annotation.
 *
 * @since 3.13.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public abstract class AbstractInlinedAnnotation extends Annotation {

	/**
	 * The type of inlined annotations.
	 */
	public static final String TYPE= "org.eclipse.jface.text.source.inlined"; //$NON-NLS-1$

	/**
	 * The position where the annotation must be drawn.
	 */
	private final Position position;

	/**
	 * The {@link StyledText} widget where the annotation must be drawn.
	 */
	private final StyledText textWidget;

	/**
	 * The dirty flag.
	 */
	private boolean dirty;

	/**
	 * Inlined annotation constructor.
	 * 
	 * @param position the position where the annotation must be drawn.
	 * @param textWidget the {@link StyledText} widget where the annotation must be drawn.
	 */
	protected AbstractInlinedAnnotation(Position position, StyledText textWidget) {
		super(TYPE, false, ""); //$NON-NLS-1$
		this.position= position;
		this.textWidget= textWidget;
	}

	/**
	 * Returns the position where the annotation must be drawn.
	 * 
	 * @return the position where the annotation must be drawn.
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Returns the {@link StyledText} widget where the annotation must be drawn.
	 * 
	 * @return the {@link StyledText} widget where the annotation must be drawn.
	 */
	public StyledText getTextWidget() {
		return textWidget;
	}

	/**
	 * Draw the inlined annotation. By default it draw the text of the annotation with gray color. User
	 * can override this method to draw anything.
	 *
	 * @param gc the graphics context
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 * @param x the x position of the annotation
	 * @param y the y position of the annotation
	 */
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		gc.setForeground(color);
		gc.setBackground(textWidget.getBackground());
		gc.drawText(getText(), x, y);
	}

	/**
	 * Returns true if the content of the annotation has changed and false otherwise.
	 * 
	 * @return true if the content of the annotation has changed and false otherwise.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Set the dirty flag.
	 * 
	 * @param dirty the dirty flag
	 */
	public void setDirty(boolean dirty) {
		this.dirty= dirty;
	}

	@Override
	public void setText(String text) {
		String oldText= super.getText();
		setDirty(!text.equals(oldText));
		super.setText(text);
	}

}
