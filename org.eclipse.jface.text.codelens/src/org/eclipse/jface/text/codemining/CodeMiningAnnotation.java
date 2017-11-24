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

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;
import org.eclipse.swt.graphics.Font;

/**
 * Code Mining annotation.
 *
 * @since 3.13.0
 */
public class CodeMiningAnnotation extends LineHeaderAnnotation {

	private final Font fFont;

	private final List<ICodeMining> fMinings;

	public CodeMiningAnnotation(Position position, ISourceViewer viewer, Font font) {
		super(position, viewer.getTextWidget());
		fFont = font;
		fMinings = new ArrayList<>();
	}

	public List<ICodeMining> getMininges() {
		return fMinings;
	}

	@Override
	public int getHeight() {
		return 20;
	}

	public void update(List<ICodeMining> minings) {
		fMinings.clear();
		fMinings.addAll(minings);
	}

	/**
	 * Returns the code mining font and null otherwise.
	 * 
	 * @return the code mining font and null otherwise.
	 */
	public Font getFont() {
		return fFont;
	}

	@Override
	public String getText() {
		String oldText = super.getText();
		super.setText(getText(new ArrayList<>(getMininges()), oldText));
		return super.getText();
	}

	private static String getText(List<ICodeMining> minings, String oldText) {
		StringBuilder text = new StringBuilder();
		for (ICodeMining codeMining : minings) {
			if (!codeMining.isResolved()) {
				// Don't render codemining which is not resolved.
				if (oldText != null) {
					return oldText;
				}
				continue;
			}
			if (text.length() > 0) {
				text.append(" | "); //$NON-NLS-1$
			}
			String title = codeMining.getCommand() != null ? codeMining.getCommand().getTitle() : "no command"; //$NON-NLS-1$
			text.append(title);
		}
		return text.toString();
	}
}
