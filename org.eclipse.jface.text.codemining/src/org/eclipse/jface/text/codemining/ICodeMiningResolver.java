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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;

/**
 * A code mining provider adds {@link Command} to source text. The commands will be shown as
 * dedicated horizontal lines in between the source text.
 * 
 * @since 3.13.0
 */
public interface ICodeMiningResolver {

	/**
	 * This function will be called for each visible content mining, usually when scrolling and after
	 * calls to compute {@link ICodeMiningProvider#provideCodeMinings(ITextViewer, IProgressMonitor)}
	 * minings.
	 * 
	 * @param viewer The viewer in which the command was invoked.
	 * 
	 * @param codeMining code mining that must be resolved.
	 * @param monitor A progress monitor.
	 * @return The given completable future that resolves to such.
	 */
	CompletableFuture<ICodeMining> resolveCodeMining(ITextViewer viewer, ICodeMining codeMining, IProgressMonitor monitor);

}
