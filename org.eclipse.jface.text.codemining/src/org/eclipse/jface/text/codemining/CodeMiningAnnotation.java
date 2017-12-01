/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.text.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;

/**
 * Code Mining annotation.
 *
 * @since 3.13
 */
public class CodeMiningAnnotation extends LineHeaderAnnotation {

	private final List<ICodeMining> fMinings;

	public CodeMiningAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer.getTextWidget());
		fMinings= new ArrayList<>();
	}

	protected List<ICodeMining> getMinings() {
		return fMinings;
	}

	public void update(List<ICodeMining> minings) {
		disposeMinings();
		fMinings.addAll(minings);
	}

	@Override
	public void markDeleted(boolean deleted) {
		super.markDeleted(deleted);
		if (deleted) {
			disposeMinings();
		}
	}

	private void disposeMinings() {
		fMinings.stream().forEach(ICodeMining::dispose);
		fMinings.clear();
	}

	@Override
	public String getText() {
		String oldText= super.getText();
		super.setText(getText(new ArrayList<>(getMinings()), oldText));
		return super.getText();
	}

	private static String getText(List<ICodeMining> minings, String oldText) {
		StringBuilder text= new StringBuilder();
		for (ICodeMining mining : minings) {
			if (!mining.isResolved()) {
				// Don't render codemining which is not resolved.
				if (oldText != null) {
					return oldText;
				}
				continue;
			}
			if (text.length() > 0) {
				text.append(" | "); //$NON-NLS-1$
			}
			String title= mining.getLabel() != null ? mining.getLabel() : "no command"; //$NON-NLS-1$
			text.append(title);
		}
		return text.toString();
	}

}
