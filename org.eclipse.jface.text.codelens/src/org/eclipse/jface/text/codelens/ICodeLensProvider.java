package org.eclipse.jface.text.codelens;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;

/**
 * A code lens provider adds [commands](#Command) to source text. The commands
 * will be shown as dedicated horizontal lines in between the source text.
 */
public interface ICodeLensProvider {

	/**
	 * Compute a list of [lenses](#CodeLens). This call should return as fast as
	 * possible and if computing the commands is expensive implementors should only
	 * return code lens objects with the range set and implement
	 * [resolve](#CodeLensProvider.resolveCodeLens).
	 *
	 * @param viewer
	 *            The viewer in which the command was invoked.
	 * @param monitor
	 *            A progress monitor.
	 * @return An array of code lenses or a thenable that resolves to such. The lack
	 *         of a result can be signaled by returning `undefined`, `null`, or an
	 *         empty array.
	 */
	CompletableFuture<Collection<ICodeLens>> provideCodeLenses(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * This function will be called for each visible code lens, usually when
	 * scrolling and after calls to
	 * [compute](#CodeLensProvider.provideCodeLenses)-lenses.
	 * 
	 * @param viewer
	 *            The viewer in which the command was invoked.
	 * 
	 * @param codeLens
	 *            code lens that must be resolved.
	 * @param monitor
	 *            A progress monitor.
	 * @return The given, resolved code lens or thenable that resolves to such.
	 */
	CompletableFuture<ICodeLens> resolveCodeLens(ITextViewer viewer, ICodeLens codeLens, IProgressMonitor monitor);

	/**
	 * Dispose code lens provider.
	 */
	void dispose();
}