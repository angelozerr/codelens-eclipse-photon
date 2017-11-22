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
package org.eclipse.jface.text.examples.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractSyncCodeMiningProvider;
import org.eclipse.jface.text.codemining.Command;
import org.eclipse.jface.text.codemining.ICodeMining;

public class ClassImplementationsCodeMiningProvider extends AbstractSyncCodeMiningProvider {

	@Override
	public List<? extends ICodeMining> provideSyncCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		List<ICodeMining> lenses = new ArrayList<>();
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			updateContentMining(i, document, "class ", lenses);
			updateContentMining(i, document, "interface ", lenses);
		}
		return lenses;
	}

	private void updateContentMining(int lineIndex, IDocument document, String token, List<ICodeMining> lenses) {
		String line = getLineText(document, lineIndex, false).trim();
		int index = line.indexOf(token);
		if (index == 0) {
			String className = line.substring(index + token.length(), line.length());
			index = className.indexOf(" ");
			if (index != -1) {
				className = className.substring(0, index);
			}
			if (className.length() > 0) {
				try {
					lenses.add(new ClassCodeMining(className, lineIndex, document, this));
				} catch (BadLocationException e) {					
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected ICodeMining resolveSyncCodeMining(ITextViewer viewer, ICodeMining contentMining, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		String className = ((ClassCodeMining) contentMining).getClassName();
		int refCount = 0;
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false);
			refCount += line.contains("implements " + className) ? 1 : 0;
		}
		((ClassCodeMining) contentMining).setCommand(new Command(refCount + " implementation", ""));
		return contentMining;
	}

	private static String getLineText(IDocument document, int line, boolean withLineDelimiter) {
		try {
			int lo = document.getLineOffset(line);
			int ll = document.getLineLength(line);
			if (!withLineDelimiter) {
				String delim = document.getLineDelimiter(line);
				ll = ll - (delim != null ? delim.length() : 0);
			}
			return document.get(lo, ll);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
