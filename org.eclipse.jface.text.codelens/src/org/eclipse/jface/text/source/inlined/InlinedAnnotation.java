package org.eclipse.jface.text.source.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class InlinedAnnotation extends Annotation {

	/**
	 * The type of inlined annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.source.inlined"; //$NON-NLS-1$

	private Integer height;

	private final Position position;

	private Integer width;

	public InlinedAnnotation(Position position, Integer height) {
		this(position, height, null);
	}

	public InlinedAnnotation(Position position, Integer height, Integer width) {
		this.position = position;
		this.height = height;
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public Position getPosition() {
		return position;
	}

}
