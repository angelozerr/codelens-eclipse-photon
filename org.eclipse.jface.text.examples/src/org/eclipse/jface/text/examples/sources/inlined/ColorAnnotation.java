package org.eclipse.jface.text.examples.sources.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.InlinedAnnotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class ColorAnnotation extends InlinedAnnotation {

	private Color color;

	public ColorAnnotation(Position pos) {
		super(pos, false);
	}

	@Override
	public Integer getHeight(StyledText styledText) {
		return null;
	}

	@Override
	public Integer getWidth(StyledText styledText) {
		return getSquareWidth(styledText);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		int size = getSquareSize(gc.getFontMetrics());
		Rectangle rect = new Rectangle(x, y, size, size);

		// Fill square
		gc.setBackground(this.color);
		gc.fillRectangle(rect);

		// Draw square box
		gc.setForeground(textWidget.getForeground());
		gc.drawRectangle(rect);
	}

	/**
	 * Returns the colorized square size.
	 * 
	 * @param fontMetrics
	 * @return the colorized square size.
	 */
	public static int getSquareSize(FontMetrics fontMetrics) {
		return fontMetrics.getHeight() - 2 * fontMetrics.getDescent();
	}

	/**
	 * Compute width of square
	 * 
	 * @param styledText
	 * @return the width of square
	 */
	private static int getSquareWidth(StyledText styledText) {
		GC gc = new GC(styledText);
		FontMetrics fontMetrics = gc.getFontMetrics();
		// width = 2 spaces + size width of square
		int width = 2 * fontMetrics.getAverageCharWidth() + getSquareSize(fontMetrics);
		gc.dispose();
		return width;
	}
}
