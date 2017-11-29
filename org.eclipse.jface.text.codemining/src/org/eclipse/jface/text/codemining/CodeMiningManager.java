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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;

/**
 * Code Mining manager implementation.
 *
 * @since 3.13.0
 */
public class CodeMiningManager implements ICodeMiningManager {

	/**
	 * The source viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * The list of codemining providers.
	 */
	private List<ICodeMiningProvider> fCodeMiningProviders;

	/**
	 * The font to use to draw the code minings annotations.
	 */
	private Font fFont;

	/**
	 * The current progress monitor.
	 */
	private IProgressMonitor fMonitor;

	/**
	 * The inlined annotation support used to draw CodeMining in the line spacing.
	 */
	private InlinedAnnotationSupport fInlinedAnnotationSupport;

	/**
	 * Preference store used to configure CodeMing providers.
	 */
	private IPreferenceStore fPreferenceStore;

	private CodeMiningPropertyChangeListener fCodeMiningPropertyChangeListener = new CodeMiningPropertyChangeListener();

	private class CodeMiningPropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().startsWith("codemining")) {
				configureProviders();
				run();
			}
		}

	}

	/**
	 * Installs this codemining manager with the given arguments.
	 * 
	 * @param viewer
	 *            the source viewer
	 * @param painter
	 *            the annotation painter to use to draw code minings
	 * @param codeMiningProviders
	 *            the array of codemining providers, must not be empty
	 */
	public void install(ISourceViewer viewer, AnnotationPainter painter, ICodeMiningProvider[] codeMiningProviders) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(painter);
		Assert.isNotNull(codeMiningProviders);
		fViewer = viewer;
		fInlinedAnnotationSupport = new InlinedAnnotationSupport();
		fInlinedAnnotationSupport.install(viewer, painter);
		setCodeMiningProviders(codeMiningProviders);
		if (fPreferenceStore != null) {
			configure(fPreferenceStore);
		}
	}

	/**
	 * Configure the CodeMining providers.
	 * 
	 * @param preferenceStore
	 */
	public void configure(IPreferenceStore preferenceStore) {
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fCodeMiningPropertyChangeListener);
		}
		fPreferenceStore = preferenceStore;
		fPreferenceStore.addPropertyChangeListener(fCodeMiningPropertyChangeListener);
		configureProviders();
	}

	private void configureProviders() {
		if (fCodeMiningProviders != null) {
			for (ICodeMiningProvider provider : fCodeMiningProviders) {
				provider.configure(fPreferenceStore);
			}
		}
	}

	/**
	 * Set the font to use to draw the code minings annotations.
	 * 
	 * @param font
	 *            the font to use to draw the code minings annotations.
	 */
	public void setFont(Font font) {
		this.fFont = font;
	}

	/**
	 * Set the codemining providers.
	 * 
	 * @param codeMiningProviders
	 *            the codemining providers.
	 */
	public void setCodeMiningProviders(ICodeMiningProvider[] codeMiningProviders) {
		fCodeMiningProviders = Arrays.asList(codeMiningProviders);
	}

	/**
	 * Uninstalls this codemining manager.
	 */
	public void uninstall() {
		cancel();
		if (fFont != null) {
			fFont.dispose();
			fFont = null;
		}
		if (fInlinedAnnotationSupport != null) {
			fInlinedAnnotationSupport.uninstall();
		}
		fViewer = null;
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fCodeMiningPropertyChangeListener);
			fPreferenceStore = null;
		}
	}

	/**
	 * Collect, resolve and render the code minings of the viewer.
	 */
	@Override
	public void run() {
		if (fViewer == null || fCodeMiningProviders == null || fViewer.getAnnotationModel() == null) {
			return;
		}
		// Cancel the last progress monitor to cancel last resolve and render of code
		// minings
		cancel();
		// Refresh the code minings by using the new progress monitor.
		fMonitor = new CodeMiningMonitor();
		update(fMonitor);
	}

	/**
	 * Update the code minings by using the given progress monitor and stop process
	 * if {@link IProgressMonitor#isCanceled()} is true.
	 * 
	 * @param monitor
	 *            the progress monitor.
	 */
	private void update(final IProgressMonitor monitor) {
		// Collect the code minings for the viewer
		getCodeMininges(fViewer, fCodeMiningProviders, monitor).thenAccept(symbols -> {
			// check if request was canceled.
			monitor.isCanceled();
			// then group code minings by lines position
			Map<Position, List<ICodeMining>> groups = goupByLines(symbols, fCodeMiningProviders);
			// resolve and render code minings
			List<CompletableFuture<ICodeMining>> codeMiningResolverRequest = renderCodeMininges(groups, fViewer,
					monitor);
			if (codeMiningResolverRequest != null) {
				for (CompletableFuture<ICodeMining> p : codeMiningResolverRequest) {
					// check if request was canceled.
					monitor.isCanceled();
					p.thenAccept(mining -> {
						// check if request was canceled.
						monitor.isCanceled();
						Position pos = mining.getPosition();
						IAnnotationModel annotationModel = fViewer.getAnnotationModel();
						Iterator<Annotation> iter = (annotationModel instanceof IAnnotationModelExtension2)
								? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(pos.offset, 1,
										true, true)
								: annotationModel.getAnnotationIterator();
						while (iter.hasNext()) {
							Annotation ann = iter.next();
							if (ann instanceof CodeMiningAnnotation) {
								// check if request was canceled.
								monitor.isCanceled();
								((CodeMiningAnnotation) ann).redraw();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Cancel the codemining process.
	 */
	private void cancel() {
		// Cancel the last progress monitor.
		if (fMonitor != null) {
			fMonitor.setCanceled(true);
		}
	}

	/**
	 * Return the list of {@link CompletableFuture} which provides the list of
	 * {@link ICodeMining} for the given <code>viewer</code> by using the given
	 * providers.
	 * 
	 * @param viewer
	 *            the text viewer.
	 * @param providers
	 *            the CodeMining list providers.
	 * @param monitor
	 *            the progress monitor.
	 * @return the list of {@link CompletableFuture} which provides the list of
	 *         {@link ICodeMining} for the given <code>viewer</code> by using the
	 *         given providers.
	 */
	private static CompletableFuture<List<? extends ICodeMining>> getCodeMininges(ITextViewer viewer,
			List<ICodeMiningProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeMining>>> com = providers.stream()
				.map(provider -> provider.provideCodeMinings(viewer, monitor))
				.filter(c -> c != null)
				.collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
	}

	/**
	 * Returns a sorted Map which groups the given code minings by same position
	 * line
	 * 
	 * @param codeMinings
	 *            list of code minings to group.
	 * @param providers
	 *            CodeMining providers used to retrieve code minings.
	 * @return a sorted Map which groups the given code minings by same position
	 *         line.
	 */
	@SuppressWarnings("unlikely-arg-type")
	private static Map<Position, List<ICodeMining>> goupByLines(List<? extends ICodeMining> codeMinings,
			List<ICodeMiningProvider> providers) {
		// sort code minings by lineNumber and provider-rank if
		Collections.sort(codeMinings, (a, b) -> {
			if (a.getPosition().offset < b.getPosition().offset) {
				return -1;
			} else if (a.getPosition().offset > b.getPosition().offset) {
				return 1;
			} else if (a.getResolver() == null && b.getResolver() != null) {
				return -1;
			} else if (a.getResolver() != null && b.getResolver() == null) {
				return 1;
			} else if (providers.indexOf(a.getResolver()) < providers.indexOf(b.getResolver())) {
				return -1;
			} else if (providers.indexOf(a.getResolver()) > providers.indexOf(b.getResolver())) {
				return 1;
			} else {
				return 0;
			}
		});
		return codeMinings.stream().collect(Collectors.groupingBy(ICodeMining::getPosition, LinkedHashMap::new,
				Collectors.mapping(Function.identity(), Collectors.toList())));
	}

	/**
	 * Render the codemining grouped by line position.
	 * 
	 * @param groups
	 *            code minings grouped by lines position
	 * @param viewer
	 *            the viewer
	 * @param monitor
	 *            the progress monitor
	 * @return the list of code minings to resolve.
	 */
	private List<CompletableFuture<ICodeMining>> renderCodeMininges(Map<Position, List<ICodeMining>> groups,
			ISourceViewer viewer, IProgressMonitor monitor) {
		// check if request was canceled.
		monitor.isCanceled();
		IDocument document = viewer != null ? viewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before codemining rendered is
			// done.
			return null;
		}
		List<CompletableFuture<ICodeMining>> miningsToResolve = new ArrayList<>();
		Set<AbstractInlinedAnnotation> currentAnnotations = new HashSet<>();
		// Loop for grouped code minings
		groups.entrySet().stream().forEach(g -> {
			Position pos = new Position(g.getKey().offset, g.getKey().length);
			List<ICodeMining> minings = g.getValue();

			// Try to find existing annotation
			CodeMiningAnnotation ann = fInlinedAnnotationSupport.findExistingAnnotation(pos);
			if (ann == null) {
				// The annotation doesn't exists, create it.
				ann = new CodeMiningAnnotation(pos, viewer, fFont);
			}
			ann.update(minings);
			// Collect minings to resolve
			for (ICodeMining mining : minings) {
				if (!mining.isResolved() && mining.getResolver() != null) {
					// mining is not resolved and it exists a resolver.
					CompletableFuture<ICodeMining> promise = mining.getResolver().resolveCodeMining(viewer, mining,
							monitor);
					// Try to resolve now
					if (promise.getNow(null) == null) {
						// It will be resolved in the "resolved" step.
						miningsToResolve.add(promise);
					}
				}
			}
			currentAnnotations.add(ann);
		});
		// check if request was canceled.
		monitor.isCanceled();
		fInlinedAnnotationSupport.updateAnnotations(currentAnnotations);
		return miningsToResolve;
	}

}
