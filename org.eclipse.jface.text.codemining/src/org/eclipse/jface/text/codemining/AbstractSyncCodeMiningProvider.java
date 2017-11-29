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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;

/**
 * Abstract class for sync code mining provider.
 *
 * @since 3.13.0
 */
public abstract class AbstractSyncCodeMiningProvider extends AbstractCodeMiningProvider implements ICodeMiningResolver {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			List<? extends ICodeMining> minings= provideSyncCodeMinings(viewer, monitor);
			return minings != null ? minings : Collections.emptyList();
		});
	}

	@Override
	public CompletableFuture<ICodeMining> resolveCodeMining(ITextViewer viewer, ICodeMining codeMining,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			return resolveSyncCodeMining(viewer, codeMining, monitor);
		});
	}

	/**
	 * Provide code minings with sync mode.
	 *
	 * @param viewer The viewer in which the command was invoked.
	 * @param monitor A progress monitor.
	 * @return An array of code minings that resolves to such. The lack of a result can be signaled
	 *         by returning `null`, or an empty array.
	 */
	protected abstract List<? extends ICodeMining> provideSyncCodeMinings(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * Resolve code mining with sync mode.
	 *
	 * @param viewer The viewer in which the command was invoked.
	 *
	 * @param codeMining code mining that must be resolved.
	 * @param monitor A progress monitor.
	 * @return The given code mining that resolves to such.
	 */
	protected abstract ICodeMining resolveSyncCodeMining(ITextViewer viewer, ICodeMining codeMining, IProgressMonitor monitor);
}
