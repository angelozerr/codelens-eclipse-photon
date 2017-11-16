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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;

/**
 * A code lens provider adds {@link Command} to source text. The commands will
 * be shown as dedicated horizontal lines in between the source text.
 * 
 * @since 3.107
 */
public interface ICodeLensResolver {

	/**
	 * This function will be called for each visible code lens, usually when
	 * scrolling and after calls to compute
	 * {@link ICodeLensProvider#provideCodeLenses(ITextViewer, IProgressMonitor)}
	 * lenses.
	 * 
	 * @param viewer
	 *            The viewer in which the command was invoked.
	 * 
	 * @param codeLens
	 *            code lens that must be resolved.
	 * @param monitor
	 *            A progress monitor.
	 * @return The given completable future that resolves to such.
	 */
	CompletableFuture<ICodeLens> resolveCodeLens(ITextViewer viewer, ICodeLens codeLens, IProgressMonitor monitor);

}
