/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Code Mining annotation.
 *
 * @since 3.13.0
 */
public class CodeMiningAnnotation extends Annotation {

	/**
	 * The type of codemining annotations.
	 */
	public static final String TYPE= "org.eclipse.jface.text.codemining"; //$NON-NLS-1$

	private final ISourceViewer fViewer;

	private final Font fFont;

	private final List<ICodeMining> fMinings;

	public CodeMiningAnnotation(ISourceViewer viewer, Font font) {
		super(TYPE, false, ""); //$NON-NLS-1$
		fViewer= viewer;
		fFont= font;
		fMinings= new ArrayList<>();
	}

	public List<ICodeMining> getMininges() {
		return fMinings;
	}

	public int getHeight() {
		return 20;
	}

	public void update(List<ICodeMining> minings) {
		fMinings.clear();
		fMinings.addAll(minings);
	}

	/**
	 * Redraw the codemining annotation.
	 */
	public void redraw() {
		StyledText text= fViewer.getTextWidget();
		CodeMiningUtilities.runInUIThread(text, (t) -> {
			Position pos= getMininges().get(0).getPosition();
			if (pos != null) {
				CodeMiningDrawingStrategy.draw(this, null, t, pos.getOffset(), pos.getLength(), null);
			}
		});
	}

	/**
	 * Returns the code mining font and null otherwise.
	 * 
	 * @return the code mining font and null otherwise.
	 */
	public Font getFont() {
		return fFont;
	}

}
