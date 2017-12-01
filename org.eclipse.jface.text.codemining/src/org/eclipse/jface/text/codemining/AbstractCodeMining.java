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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.Positions;

/**
 * Abstract class for {@link ICodeMining}.
 *
 * @since 3.13
 */
public abstract class AbstractCodeMining implements ICodeMining {

	/**
	 * The position where codemining must be drawn
	 */
	private final Position position;

	private final ICodeMiningProvider provider;

	private CompletableFuture<Void> resolveFuture;

	/**
	 * The label of the resolved codemining.
	 */
	private String label;

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 *
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @throws BadLocationException when line number doesn't exists
	 */
	public AbstractCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider)
			throws BadLocationException {
		this.position= Positions.of(beforeLineNumber, document, true);
		this.provider= provider;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public ICodeMiningProvider getProvider() {
		return provider;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label= label;
	}

	@Override
	public final CompletableFuture<Void> resolve(ITextViewer viewer, IProgressMonitor monitor) {
		if (resolveFuture == null) {
			resolveFuture= doResolve(viewer, monitor);
		}
		return resolveFuture;
	}

	/**
	 * Returns the future which resolved the content of mining and null otherwise. By default, the
	 * resolve do nothing.
	 *
	 * @param viewer the viewer
	 * @param monitor the monitor
	 * @return the future which resolved the content of mining and null otherwise.
	 */
	protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public boolean isResolved() {
		return resolveFuture == null || resolveFuture.isDone() || label != null;
	}

	@Override
	public void dispose() {
		if (resolveFuture != null) {
			resolveFuture.cancel(true);
			resolveFuture= null;
		}
	}
}
