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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

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
	 * Returns the label may be set early in the class lifecycle, or upon completion of the future
	 * provided by {@link #resolve(ITextViewer)} operation.
	 *
	 * @return the label may be set early in the class lifecycle, or upon completion of the future
	 *         provided by {@link #resolve(ITextViewer)} operation.
	 */
	String getLabel();

	/**
	 * Set the progress monitor used when {@link #resolve(ITextViewer)} is done.
	 *
	 * @param monitor the progress monitor used when {@link #resolve(ITextViewer)} is done.
	 */
	void setProgressMonitor(IProgressMonitor monitor);

	/**
	 * Returns the future to resolve the content of mining, or
	 * {@link CompletableFuture#completedFuture(Object)} if no such resolution is necessary (in
	 * which case {#isResolved()} is expected to return <code>true</code>).
	 *
	 * @param viewer the viewer.
	 * @return the future to resolve the content of mining, or
	 *         {@link CompletableFuture#completedFuture(Object)} if no such resolution is necessary
	 *         (in which case {#isResolved()} is expected to return <code>true</code>).
	 */
	CompletableFuture<Void> resolve(ITextViewer viewer);

	/**
	 * Returns whether the content mining is resolved. If it is not resolved,
	 * {{@link #resolve(ITextViewer)}} will be invoked later, triggering the future to resolve
	 * content.
	 *
	 * @return whether the content mining is resolved. If it is not resolved,
	 *         {{@link #resolve(ITextViewer)}} will be invoked later, triggering the future to
	 *         resolve content.
	 */
	boolean isResolved();

	/**
	 * Draw the code mining.
	 *
	 * @param gc the graphics context
	 * @param textWidget the text widget to draw on
	 * @param color the color of the line
	 * @param x the x position of the annotation
	 * @param y the y position of the annotation
	 * @return the size of the draw of mining.
	 */
	Point draw(GC gc, StyledText textWidget, Color color, int x, int y);

	/**
	 * Dispose the mining. Typically shuts down or cancels all related asynchronous operations.
	 */
	void dispose();
}
