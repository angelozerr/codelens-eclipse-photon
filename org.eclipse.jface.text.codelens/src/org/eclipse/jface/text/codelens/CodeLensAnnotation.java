/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeLens support - Bug XXXXXX
 */
package org.eclipse.jface.text.codelens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;

/**
 * CodeLens annotation.
 *
 */
public class CodeLensAnnotation extends Annotation {

	/**
	 * The type of codelens annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.codelens"; //$NON-NLS-1$

	private final Font font;
	private final List<ICodeLens> lenses;

	public CodeLensAnnotation(Font font) {
		super(TYPE, false, "");
		this.font = font;
		this.lenses = new ArrayList<>();
	}

	public List<ICodeLens> getLenses() {
		return lenses;
	}

	public int getHeight() {
		return 20;
	}

	public void update(List<ICodeLens> lenses) {
		this.lenses.clear();
		this.lenses.addAll(lenses);
	}

	public void redraw(StyledText text) {
		CodeLensHelper.runInUIThread(text,
				(t) -> CodeLensDrawingStrategy.draw(this, null, t, getLenses().get(0).getPosition().offset, 1, null));
	}

	/**
	 * Returns the lens font and null otherwise.
	 * 
	 * @return the lens font and null otherwise.
	 */
	public Font getFont() {
		return font;
	}

	public boolean isFirstLine(StyledText textWidget) {
		return getLenses().get(0).getPosition().offset == 0;
	}
}
