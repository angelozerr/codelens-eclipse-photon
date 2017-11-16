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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;

/**
 * Abstract class for sync code lens provider.
 *
 * @since 3.107
 */
public abstract class AbstractSyncCodeLensProvider extends AbstractCodeLensProvider implements ICodeLensResolver {

	@Override
	public CompletableFuture<List<? extends ICodeLens>> provideCodeLenses(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			return provideSyncCodeLenses(viewer, monitor);
		});
	}

	@Override
	public CompletableFuture<ICodeLens> resolveCodeLens(ITextViewer viewer, ICodeLens codeLens,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			return resolveSyncCodeLens(viewer, codeLens, monitor);
		});
	}

	/**
	 * 
	 * @param viewer
	 * @param monitor
	 * @return
	 */
	protected abstract List<? extends ICodeLens> provideSyncCodeLenses(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * 
	 * @param viewer
	 * @param codeLens
	 * @param monitor
	 * @return
	 */
	protected abstract ICodeLens resolveSyncCodeLens(ITextViewer viewer, ICodeLens codeLens, IProgressMonitor monitor);
}
