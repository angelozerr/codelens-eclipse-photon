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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
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

	List<CompletableFuture<ICodeLens>> promises;

	private ISourceViewer viewer;
	private StyledText textWidget;

	private boolean alreadyDone;

	private boolean disposed;

	public CodeLensAnnotation() {
		super(TYPE, false, "");
		this.lenses = new ArrayList<>();
	}

	public List<ICodeLens> getLenses() {
		if (promises != null && !alreadyDone) {
			alreadyDone = true;
			List<CompletableFuture<ICodeLens>> pr = new ArrayList<>(promises);
			for (CompletableFuture<ICodeLens> p : pr) {
				ICodeLens l = p.getNow(null);
				if (l != null) {
					textWidget.getDisplay().asyncExec(() -> {
						if (p.isCancelled()) {
							promises.remove(p);
							return;
						}
						promises.remove(p);
						Position pos = viewer.getAnnotationModel().getPosition(this);
						if (pos != null) {
							CodeLensDrawingStrategy.draw(this, null, textWidget, pos.offset, 1, null);
						}
					});
				}
				else {
				p.thenAcceptAsync(lens -> {
					if (p.isCancelled()) {
						promises.remove(p);
						return;
					}
					textWidget.getDisplay().asyncExec(() -> {
						if (p.isCancelled()) {
							promises.remove(p);
							return;
						}
						Position pos = viewer.getAnnotationModel().getPosition(this);
						if (pos != null) {
							CodeLensDrawingStrategy.draw(this, null, textWidget, pos.offset, 1, null);
						}
					});
				});
				}
			}

		}
		return lenses;

	}

	public int getHeight() {
		return 20;
	}

	public void setCodeLenses(List<ICodeLens> lenses, ITextViewer viewer, IProgressMonitor monitor) {
		this.lenses.clear();
		this.lenses.addAll(lenses);
		if (promises != null) {
			for (CompletableFuture<ICodeLens> p : promises) {
				p.cancel(true);
			}
		}
		alreadyDone = false;
		this.viewer = (ISourceViewer) viewer;
		textWidget = viewer.getTextWidget();
		promises = null;
		//lenses.stream().filter(lens -> !lens.isResolved() && lens.getResolver() != null)
		//		.map(lens -> lens.getResolver().resolveCodeLens(viewer, lens, monitor)).collect(Collectors.toList());
	}

	public static <R> CompletableFuture<R> computeAsync(Function<IProgressMonitor, R> code) {
		CompletableFuture start = new CompletableFuture();
		CompletableFuture result = start.thenApplyAsync(code);
		IProgressMonitor cancelIndicator = new NullProgressMonitor() {
			@Override
			public boolean isCanceled() {
				if (result.isCancelled()) {
					throw new CancellationException();
				}
				return false;
			}
		};
		start.complete(cancelIndicator);
		return result;
	}

	public void dispose() {
		// disposed = true;
		if (promises != null) {
			for (CompletableFuture<ICodeLens> p : promises) {
				p.cancel(true);
			}
		}
	}

	public boolean isDisposed() {
		return disposed;
	}
}
