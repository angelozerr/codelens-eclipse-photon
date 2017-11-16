/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - CodeLens support - Bug 526969
 */
package org.eclipse.jface.text.codelens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;

/**
 * CodeLens annotation.
 *
 * @since 3.107
 */
public class CodeLensAnnotation extends Annotation {

	/**
	 * The type of codelens annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.codelens"; //$NON-NLS-1$

	private final ISourceViewer viewer;
	private final Font font;
	private final List<ICodeLens> lenses;

	public CodeLensAnnotation(ISourceViewer viewer, Font font) {
		super(TYPE, false, "");
		this.viewer = viewer;
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

	/**
	 * Redraw the codelens annotation.
	 */
	public void redraw() {
		StyledText text = viewer.getTextWidget();
		CodeLensUtilities.runInUIThread(text, (t) -> {
			Position pos = getLenses().get(0).getPosition();
			if (pos != null) {
				CodeLensDrawingStrategy.draw(this, null, t, pos.getOffset(), pos.getLength(), null);
			}
		});
	}

	/**
	 * Returns the lens font and null otherwise.
	 * 
	 * @return the lens font and null otherwise.
	 */
	public Font getFont() {
		return font;
	}

}
