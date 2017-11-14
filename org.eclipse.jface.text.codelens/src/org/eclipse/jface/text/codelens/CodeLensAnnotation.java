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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;

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

	public void setCodeLenses(List<ICodeLens> lenses, ITextViewer viewer, IProgressMonitor monitor) {
		this.lenses.clear();
		this.lenses.addAll(lenses);
	}
}
