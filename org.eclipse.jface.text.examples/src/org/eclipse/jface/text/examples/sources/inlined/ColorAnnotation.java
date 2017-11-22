package org.eclipse.jface.text.examples.sources.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.InlinedAnnotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class ColorAnnotation extends InlinedAnnotation {

	private Color color;

	public ColorAnnotation(Position pos) {
		super(pos, false);
	}

	@Override
	public Integer getHeight() {
		return null;
	}

	@Override
	public Integer getWidth() {
		return 20;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		int size = 20; //ColorSymbolSupport.getSquareSize(fontMetrics);
		Rectangle rect = new Rectangle(x, y, size, size);

		// Fill square
		gc.setBackground(this.color);
		gc.fillRectangle(rect);

		// Draw square box
		gc.setForeground(textWidget.getForeground());
		gc.drawRectangle(rect);
	}


}
