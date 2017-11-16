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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * CodeLens manager.
 *
 * @since 3.107
 */
public class CodeLensManager implements Runnable, StyledTextLineSpacingProvider {

	/**
	 * The annotation codelens strategy singleton.
	 */
	private static final IDrawingStrategy CODELENS_STRATEGY = new CodeLensDrawingStrategy();

	/**
	 * The annotation codelens strategy ID.
	 */
	private static final String CODELENS_STRATEGY_ID = "codelens";

	/**
	 * The source viewer
	 */
	private ISourceViewer viewer;

	/**
	 * The annotation painter to use to draw the codelens.
	 */
	private AnnotationPainter painter;

	/**
	 * The list of codelens providers.
	 */
	private List<ICodeLensProvider> codeLensProviders;

	/**
	 * Holds the current lens annotations.
	 */
	private List<CodeLensAnnotation> codeLensAnnotations = null;

	/**
	 * The font to use to draw the lenses annotations.
	 */
	private Font font;

	/**
	 * The current progress monitor.
	 */
	private IProgressMonitor monitor;

	/**
	 * Installs this codelens manager with the given arguments.
	 * 
	 * @param viewer
	 *            the source viewer
	 * @param painter
	 *            the annotation painter to use to draw lenses
	 * @param codeLensProviders
	 *            the array of codelens providers, must not be empty
	 */
	public void install(ISourceViewer viewer, AnnotationPainter painter, ICodeLensProvider[] codeLensProviders) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(painter);
		Assert.isNotNull(codeLensProviders);
		this.viewer = viewer;
		this.painter = painter;
		initPainter(painter);
		setCodeLensProviders(codeLensProviders);
		StyledText text = this.viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		// Initialize defaut lens font and color.
		FontData[] fds = text.getFont().getFontData();
		for (int i = 0; i < fds.length; i++) {
			fds[i].setStyle(fds[i].getStyle() | SWT.ITALIC);
		}
		setFont(new Font(text.getDisplay(), fds));
		setColor(text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		text.setLineSpacingProvider(this);
	}

	/**
	 * Initialize painter with lens drawing strategy.
	 * 
	 * @param painter
	 *            the annotation painter to initialize with lens drawing strategy.
	 */
	private void initPainter(AnnotationPainter painter) {
		painter.addDrawingStrategy(CODELENS_STRATEGY_ID, CODELENS_STRATEGY);
		painter.addAnnotationType(CodeLensAnnotation.TYPE, CODELENS_STRATEGY_ID);
	}

	/**
	 * Set the font to use to draw the lenses annotations.
	 * 
	 * @param font
	 *            the font to use to draw the lenses annotations.
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * Set the color to use to draw the lenses annotations.
	 * 
	 * @param color
	 *            the color to use to draw the lenses annotations.
	 */
	public void setColor(Color color) {
		painter.setAnnotationTypeColor(CodeLensAnnotation.TYPE, color);
	}

	/**
	 * Set the codelens providers.
	 * 
	 * @param codeLensProviders
	 *            the codelens providers.
	 */
	public void setCodeLensProviders(ICodeLensProvider[] codeLensProviders) {
		this.codeLensProviders = Arrays.asList(codeLensProviders);
	}

	/**
	 * Uninstalls this codelens manager.
	 */
	public void uninstall() {
		cancel();
		if (font != null) {
			font.dispose();
			font = null;
		}
	}

	/**
	 * Collect, resolve and render the lenses of the viewer.
	 */
	@Override
	public void run() {
		if (viewer == null || codeLensProviders == null || viewer.getAnnotationModel() == null) {
			return;
		}
		// Cancel the last progress monitor to cancel last resolve and render of lenses
		cancel();
		// Refresh the lenses by using the new progress monitor.
		monitor = new CodeLensMonitor();
		update(monitor);
	}

	/**
	 * Update the lenses by using the given progress monitor and stop process if
	 * {@link IProgressMonitor#isCanceled()} is true.
	 * 
	 * @param monitor
	 *            the progress monitor.
	 */
	private void update(final IProgressMonitor monitor) {
		// Collect the lenses for the viewer
		getCodeLenses(viewer, codeLensProviders, monitor).thenAccept(symbols -> {
			// check if request was canceled.
			monitor.isCanceled();
			// then group lenses by lines position
			Map<Position, List<ICodeLens>> groups = goupByLines(symbols, codeLensProviders);
			// resolve and render lenses
			List<CompletableFuture<ICodeLens>> codeLensResolverRequest = renderCodeLenses(groups, viewer, monitor);
			if (codeLensResolverRequest != null) {
				for (CompletableFuture<ICodeLens> p : codeLensResolverRequest) {
					// check if request was canceled.
					monitor.isCanceled();
					p.thenAccept(lens -> {
						// check if request was canceled.
						monitor.isCanceled();
						Position pos = lens.getPosition();
						IAnnotationModel annotationModel = viewer.getAnnotationModel();
						Iterator<Annotation> iter = (annotationModel instanceof IAnnotationModelExtension2)
								? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(pos.offset, 1,
										true, true)
								: annotationModel.getAnnotationIterator();
						while (iter.hasNext()) {
							Annotation ann = iter.next();
							if (ann instanceof CodeLensAnnotation) {
								// check if request was canceled.
								monitor.isCanceled();
								((CodeLensAnnotation) ann).redraw();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Cancel the codelens process.
	 */
	private void cancel() {
		// Cancel the last progress monitor.
		if (monitor != null) {
			monitor.setCanceled(true);
		}
	}

	/**
	 * Return the list of {@link CompletableFuture} which provides the list of
	 * {@link ICodeLens} for the given <code>viewer</code> by using the given
	 * providers.
	 * 
	 * @param viewer
	 *            the text viewer.
	 * @param providers
	 *            the CodeLens list providers.
	 * @param monitor
	 *            the progress monitor.
	 * @return the list of {@link CompletableFuture} which provides the list of
	 *         {@link ICodeLens} for the given <code>viewer</code> by using the
	 *         given providers.
	 */
	private static CompletableFuture<List<? extends ICodeLens>> getCodeLenses(ITextViewer viewer,
			List<ICodeLensProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeLens>>> com = providers.stream()
				.map(provider -> provider.provideCodeLenses(viewer, monitor)).collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
	}

	/**
	 * Returns a sorted Map which groups the given lenses by same position line
	 * 
	 * @param lenses
	 *            list of lenses to group.
	 * @param providers
	 *            CodeLens providers used to retrieve lenses.
	 */
	private static Map<Position, List<ICodeLens>> goupByLines(List<? extends ICodeLens> lenses,
			List<ICodeLensProvider> providers) {
		// sort lenses by lineNumber and provider-rank if
		Collections.sort(lenses, (a, b) -> {
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
		return lenses.stream().collect(Collectors.groupingBy(ICodeLens::getPosition, LinkedHashMap::new,
				Collectors.mapping(Function.identity(), Collectors.toList())));
	}

	/**
	 * Render the codelens grouped by line position.
	 * 
	 * @param groups
	 *            lenses grouped by lines position
	 * @param viewer
	 *            the viewer
	 * @param monitor
	 *            the progress monitot
	 * @return the list of lenses to resolve.
	 */
	private List<CompletableFuture<ICodeLens>> renderCodeLenses(Map<Position, List<ICodeLens>> groups,
			ISourceViewer viewer, IProgressMonitor monitor) {
		// check if request was canceled.
		monitor.isCanceled();
		IDocument document = viewer != null ? viewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before codelens rendered is done.
			return null;
		}
		List<CompletableFuture<ICodeLens>> lensesToResolve = new ArrayList<>();
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		Map<Annotation, Position> annotationsToAdd = new HashMap<>();
		// Initialize annotations to delete with last annotations
		List<Annotation> annotationsToRemove = codeLensAnnotations != null ? new ArrayList<>(codeLensAnnotations)
				: Collections.emptyList();
		List<CodeLensAnnotation> currentAnnotations = new ArrayList<>();
		// Loop for grouped lenses
		groups.entrySet().stream().forEach(g -> {
			Position pos = new Position(g.getKey().offset, g.getKey().length);
			List<ICodeLens> lenses = g.getValue();

			// Try to find existing annotation
			CodeLensAnnotation ann = findExistingAnnotation(pos, annotationModel);
			if (ann == null) {
				// The annotation doesn't exists, create it.
				ann = new CodeLensAnnotation(viewer, font);
				annotationsToAdd.put(ann, pos);
			} else {
				// The annotation exists, remove it from the list to delete.
				annotationsToRemove.remove(ann);
			}
			ann.update(lenses);
			// Collect lenses to resolve
			for (ICodeLens lens : lenses) {
				if (!lens.isResolved() && lens.getResolver() != null) {
					// lens is not resolved and it exists a resolver.
					CompletableFuture<ICodeLens> promise = lens.getResolver().resolveCodeLens(viewer, lens, monitor);
					// Try to resolve now
					if (promise.getNow(null) == null) {
						// It will be resolved in the "resolved" step.
						lensesToResolve.add(promise);
					}
				}
			}
			currentAnnotations.add(ann);
		});
		// check if request was canceled.
		monitor.isCanceled();
		// Mark annotation as deleted to ignore the draw
		for (Annotation ann : annotationsToRemove) {
			ann.markDeleted(true);
		}
		// Update annotation model
		synchronized (getLockObject(annotationModel)) {
			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of codelens
				// range
				// (ex: user key press
				// "Enter"), but we don't neeod to redraw the viewer because change of position
				// is done by AnnotationPainter.
			} else {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(
							annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]), annotationsToAdd);
				} else {
					removeCodeLensAnnotations();
					Iterator<Entry<Annotation, Position>> iter = annotationsToAdd.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<Annotation, Position> mapEntry = iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
			codeLensAnnotations = currentAnnotations;
		}
		return lensesToResolve;
	}

	/**
	 * Returns the existing codelens annotation with the given position information
	 * and null otherwise.
	 * 
	 * @param pos
	 *            the position
	 * @param annotationModel
	 *            the annotation model.
	 * @return the existing codelens annotation with the given position information
	 *         and null otherwise.
	 */
	private CodeLensAnnotation findExistingAnnotation(Position pos, IAnnotationModel annotationModel) {
		if (codeLensAnnotations == null) {
			return null;
		}
		for (Annotation annotation : codeLensAnnotations) {
			CodeLensAnnotation ann = (CodeLensAnnotation) annotation;
			Position p = annotationModel.getPosition(annotation);
			if (p != null && p.offset == pos.offset) {
				return ann;
			}
		}
		return null;
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel
	 *            the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Remove the codelens annotations.
	 */
	private void removeCodeLensAnnotations() {

		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null || codeLensAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						codeLensAnnotations.toArray(new Annotation[codeLensAnnotations.size()]), null);
			} else {
				for (Annotation fColorSymbolAnnotation : codeLensAnnotations)
					annotationModel.removeAnnotation(fColorSymbolAnnotation);
			}
			codeLensAnnotations = null;
		}
	}

	/**
	 * Returns the line spacing from the given line index with the codelens
	 * annotations height and null otherwise.
	 */
	@Override
	public Integer getLineSpacing(int lineIndex) {
		CodeLensAnnotation annotation = CodeLensUtilities.getCodeLensAnnotationAtLine(viewer, lineIndex);
		return annotation != null ? annotation.getHeight() : null;
	}

}
