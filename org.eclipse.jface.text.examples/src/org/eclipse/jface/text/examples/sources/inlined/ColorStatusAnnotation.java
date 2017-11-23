package org.eclipse.jface.text.examples.sources.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.InlinedAnnotation;
import org.eclipse.swt.custom.StyledText;

public class ColorStatusAnnotation extends InlinedAnnotation {

	public ColorStatusAnnotation(Position position) {
		super(position, true);
	}

	@Override
	public Integer getHeight(StyledText styledText) {
		return 20;
	}

	@Override
	public Integer getWidth(StyledText styledText) {
		return null;
	}
	
	public void setStatus(String status) {
		super.setText(status);
	}

}
