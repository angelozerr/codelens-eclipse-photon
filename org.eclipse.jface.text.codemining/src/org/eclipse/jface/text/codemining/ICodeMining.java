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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;

/**
 * A code mining represents a content (ex: label, icons) that should be shown along with source
 * text, like the number of references, a way to run tests (with run/debug icons), etc.
 *
 * A code mining is unresolved when no content (ex: label, icons) is associated to it. For
 * performance reasons the creation of a code mining and resolving should be done to two stages.
 *
 * @since 3.13
 */
public interface ICodeMining {

	/**
	 * Returns the line position where code mining must be displayed in the line spacing area.
	 *
	 * @return the line position where code mining must be displayed in the line spacing area.
	 */
	Position getPosition();

	/**
	 * Returns the owner provider which has created this mining.
	 *
	 * @return the owner provider which has created this mining.
	 */
	ICodeMiningProvider getProvider();

	/**
	 * Returns the resolved label.
	 *
	 * @return the resolved label.
	 */
	String getLabel();

	/**
	 * Returns the future which resolved the content of mining and null otherwise.
	 *
	 * @param viewer the viewer.
	 * @param monitor the monitor.
	 * @return the future which resolved the content of mining and null otherwise.
	 */
	CompletableFuture<Void> resolve(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * Returns true if the content mining is resolved and false otherwise.
	 *
	 * @return true if the content mining is resolved and false otherwise.
	 */
	boolean isResolved();

	/**
	 * Dispose the mining.
	 */
	void dispose();
}
