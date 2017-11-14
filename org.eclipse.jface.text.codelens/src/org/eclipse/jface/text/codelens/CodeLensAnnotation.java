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

/**
 * CodeLens annotation.
 *
 */
public class CodeLensAnnotation extends Annotation {

	/**
	 * The type of codelens annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.codelens"; //$NON-NLS-1$

	private final List<ICodeLens> lenses;

	public CodeLensAnnotation() {
		super(TYPE, false, "");
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
		if (text == null || text.isDisposed()) {
			return;
		}
		text.getDisplay().asyncExec(() -> {
			if (text.isDisposed()) {
				return;
			}
			CodeLensDrawingStrategy.draw(this, null, text, getLenses().get(0).getPosition().offset, 1, null);
		});
	}
}
