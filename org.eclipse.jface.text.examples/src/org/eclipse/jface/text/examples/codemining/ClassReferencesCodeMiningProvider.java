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
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractSyncCodeMiningProvider;
import org.eclipse.jface.text.codemining.Command;
import org.eclipse.jface.text.codemining.ICodeMining;

public class ClassReferencesCodeMiningProvider extends AbstractSyncCodeMiningProvider {

	private Object lock = new Object();

	@Override
	protected List<? extends ICodeMining> provideSyncCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		List<ICodeMining> lenses = new ArrayList<>();
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false).trim();
			int index = line.indexOf("class ");
			if (index == 0) {
				String className = line.substring(index + "class ".length(), line.length()).trim();				
				if (className.length() > 0) {
					try {
						lenses.add(new ClassCodeMining(className, i, document, this));
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return lenses;
	}

	@Override
	protected ICodeMining resolveSyncCodeMining(ITextViewer viewer, ICodeMining contentMining, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		String className = ((ClassCodeMining) contentMining).getClassName();
		try {
			int wait = Integer.parseInt(className);
			try {

				for (int i = 0; i < wait; i++) {
					monitor.isCanceled();
					synchronized (lock) {
						lock.wait(1000);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (NumberFormatException e) {

		} catch (CancellationException e) {
			e.printStackTrace();
			throw e;
		}

		int refCount = 0;
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false);
			refCount += line.contains("new " + className) ? 1 : 0;
		}
		((ClassCodeMining) contentMining).setCommand(new Command(refCount + " references", ""));
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
