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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;

/**
 * A code mining provider adds commands {@link Command} to source text. The commands will be shown
 * as dedicated horizontal lines in between the source text.
 * 
 * @since 3.13.0
 */
public interface ICodeMiningProvider {

	/**
	 * Compute a list of code minings {@link ICodeMining}. This call should return as fast as possible
	 * and if computing the commands is expensive implementors should only return code mining objects
	 * with the range set and implement resolve
	 * {@link ICodeMiningResolver#resolveCodeMining(ITextViewer, ICodeMining, IProgressMonitor)}.
	 *
	 * @param viewer The viewer in which the command was invoked.
	 * @param monitor A progress monitor.
	 * @return An array of completable future of code minings that resolves to such. The lack of a
	 *         result can be signaled by returning `null`, or an empty array.
	 */
	CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * Dispose code mining provider.
	 */
	void dispose();
}
