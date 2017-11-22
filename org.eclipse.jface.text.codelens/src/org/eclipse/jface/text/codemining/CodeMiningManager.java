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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

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

/**
 * Code Mining manager implementation.
 *
 * @since 3.13.0
 */
public class CodeMiningManager implements ICodeMiningManager, StyledTextLineSpacingProvider {

	/**
	 * The annotation codemining strategy singleton.
	 */
	private static final IDrawingStrategy CODEMINING_STRATEGY= new CodeMiningDrawingStrategy();

	/**
	 * The annotation codemining strategy ID.
	 */
	private static final String CODEMINING_STRATEGY_ID= "codemining"; //$NON-NLS-1$

	/**
	 * The source viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * The annotation painter to use to draw the codemining.
	 */
	private AnnotationPainter fPainter;

	/**
	 * The list of codemining providers.
	 */
	private List<ICodeMiningProvider> fCodeMiningProviders;

	/**
	 * Holds the current code mining annotations.
	 */
	private List<CodeMiningAnnotation> fCodeMiningAnnotations= null;

	/**
	 * The font to use to draw the code minings annotations.
	 */
	private Font fFont;

	/**
	 * The current progress monitor.
	 */
	private IProgressMonitor fMonitor;

	/**
	 * Installs this codemining manager with the given arguments.
	 * 
	 * @param viewer the source viewer
	 * @param painter the annotation painter to use to draw code minings
	 * @param codeMiningProviders the array of codemining providers, must not be empty
	 */
	public void install(ISourceViewer viewer, AnnotationPainter painter, ICodeMiningProvider[] codeMiningProviders) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(painter);
		Assert.isNotNull(codeMiningProviders);
		fViewer= viewer;
		fPainter= painter;
		initPainter(painter);
		setCodeMiningProviders(codeMiningProviders);
		StyledText text= this.fViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		// Initialize defaut code mining font and color.
		FontData[] fds= text.getFont().getFontData();
		for (int i= 0; i < fds.length; i++) {
			fds[i].setStyle(fds[i].getStyle() | SWT.ITALIC);
		}
		setFont(new Font(text.getDisplay(), fds));
		setColor(text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		text.setLineSpacingProvider(this);
	}

	/**
	 * Initialize painter with code mining drawing strategy.
	 * 
	 * @param painter the annotation painter to initialize with code mining drawing strategy.
	 */
	private void initPainter(AnnotationPainter painter) {
		painter.addDrawingStrategy(CODEMINING_STRATEGY_ID, CODEMINING_STRATEGY);
		painter.addAnnotationType(CodeMiningAnnotation.TYPE, CODEMINING_STRATEGY_ID);
	}

	/**
	 * Set the font to use to draw the code minings annotations.
	 * 
	 * @param font the font to use to draw the code minings annotations.
	 */
	public void setFont(Font font) {
		this.fFont= font;
	}

	/**
	 * Set the color to use to draw the code minings annotations.
	 * 
	 * @param color the color to use to draw the code minings annotations.
	 */
	public void setColor(Color color) {
		fPainter.setAnnotationTypeColor(CodeMiningAnnotation.TYPE, color);
	}

	/**
	 * Set the codemining providers.
	 * 
	 * @param codeMiningProviders the codemining providers.
	 */
	public void setCodeMiningProviders(ICodeMiningProvider[] codeMiningProviders) {
		fCodeMiningProviders= Arrays.asList(codeMiningProviders);
	}

	/**
	 * Uninstalls this codemining manager.
	 */
	public void uninstall() {
		cancel();
		if (fFont != null) {
			fFont.dispose();
			fFont= null;
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
		// Cancel the last progress monitor to cancel last resolve and render of code minings
		cancel();
		// Refresh the code minings by using the new progress monitor.
		fMonitor= new CodeMiningMonitor();
		update(fMonitor);
	}

	/**
	 * Update the code minings by using the given progress monitor and stop process if
	 * {@link IProgressMonitor#isCanceled()} is true.
	 * 
	 * @param monitor the progress monitor.
	 */
	private void update(final IProgressMonitor monitor) {
		// Collect the code minings for the viewer
		getCodeMininges(fViewer, fCodeMiningProviders, monitor).thenAccept(symbols -> {
			// check if request was canceled.
			monitor.isCanceled();
			// then group code minings by lines position
			Map<Position, List<ICodeMining>> groups= goupByLines(symbols, fCodeMiningProviders);
			// resolve and render code minings
			List<CompletableFuture<ICodeMining>> codeMiningResolverRequest= renderCodeMininges(groups, fViewer, monitor);
			if (codeMiningResolverRequest != null) {
				for (CompletableFuture<ICodeMining> p : codeMiningResolverRequest) {
					// check if request was canceled.
					monitor.isCanceled();
					p.thenAccept(mining -> {
						// check if request was canceled.
						monitor.isCanceled();
						Position pos= mining.getPosition();
						IAnnotationModel annotationModel= fViewer.getAnnotationModel();
						Iterator<Annotation> iter= (annotationModel instanceof IAnnotationModelExtension2)
								? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(pos.offset, 1,
										true, true)
								: annotationModel.getAnnotationIterator();
						while (iter.hasNext()) {
							Annotation ann= iter.next();
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
	 * Return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining} for
	 * the given <code>viewer</code> by using the given providers.
	 * 
	 * @param viewer the text viewer.
	 * @param providers the CodeMining list providers.
	 * @param monitor the progress monitor.
	 * @return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining} for
	 *         the given <code>viewer</code> by using the given providers.
	 */
	private static CompletableFuture<List<? extends ICodeMining>> getCodeMininges(ITextViewer viewer,
			List<ICodeMiningProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeMining>>> com= providers.stream()
				.map(provider -> provider.provideCodeMinings(viewer, monitor)).collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
	}

	/**
	 * Returns a sorted Map which groups the given code minings by same position line
	 * 
	 * @param codeMinings list of code minings to group.
	 * @param providers CodeMining providers used to retrieve code minings.
	 * @return a sorted Map which groups the given code minings by same position line.
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
	 * @param groups code minings grouped by lines position
	 * @param viewer the viewer
	 * @param monitor the progress monitor
	 * @return the list of code minings to resolve.
	 */
	private List<CompletableFuture<ICodeMining>> renderCodeMininges(Map<Position, List<ICodeMining>> groups,
			ISourceViewer viewer, IProgressMonitor monitor) {
		// check if request was canceled.
		monitor.isCanceled();
		IDocument document= viewer != null ? viewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before codemining rendered is done.
			return null;
		}
		List<CompletableFuture<ICodeMining>> miningsToResolve= new ArrayList<>();
		IAnnotationModel annotationModel= viewer.getAnnotationModel();
		Map<Annotation, Position> annotationsToAdd= new HashMap<>();
		// Initialize annotations to delete with last annotations
		List<Annotation> annotationsToRemove= fCodeMiningAnnotations != null
				? new ArrayList<>(fCodeMiningAnnotations)
				: Collections.emptyList();
		List<CodeMiningAnnotation> currentAnnotations= new ArrayList<>();
		// Loop for grouped code minings
		groups.entrySet().stream().forEach(g -> {
			Position pos= new Position(g.getKey().offset, g.getKey().length);
			List<ICodeMining> minings= g.getValue();

			// Try to find existing annotation
			CodeMiningAnnotation ann= findExistingAnnotation(pos, annotationModel);
			if (ann == null) {
				// The annotation doesn't exists, create it.
				ann= new CodeMiningAnnotation(viewer, fFont);
				annotationsToAdd.put(ann, pos);
			} else {
				// The annotation exists, remove it from the list to delete.
				annotationsToRemove.remove(ann);
			}

			ann.update(minings);
			// Collect minings to resolve
			for (ICodeMining mining : minings) {
				if (!mining.isResolved() && mining.getResolver() != null) {
					// mining is not resolved and it exists a resolver.
					CompletableFuture<ICodeMining> promise= mining.getResolver().resolveCodeMining(viewer, mining, monitor);
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
		// Mark annotation as deleted to ignore the draw
		for (Annotation ann : annotationsToRemove) {
			ann.markDeleted(true);
		}
		// Update annotation model
		synchronized (getLockObject(annotationModel)) {
			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of codemining
				// range
				// (ex: user key press
				// "Enter"), but we don't neeod to redraw the viewer because change of position
				// is done by AnnotationPainter.
			} else {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(
							annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]), annotationsToAdd);
				} else {
					removeCodeMiningAnnotations();
					Iterator<Entry<Annotation, Position>> iter= annotationsToAdd.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<Annotation, Position> mapEntry= iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
			fCodeMiningAnnotations= currentAnnotations;
		}
		return miningsToResolve;
	}

	/**
	 * Returns the existing codemining annotation with the given position information and null
	 * otherwise.
	 * 
	 * @param pos the position
	 * @param annotationModel the annotation model.
	 * @return the existing codemining annotation with the given position information and null
	 *         otherwise.
	 */
	private CodeMiningAnnotation findExistingAnnotation(Position pos, IAnnotationModel annotationModel) {
		if (fCodeMiningAnnotations == null) {
			return null;
		}
		for (Annotation annotation : fCodeMiningAnnotations) {
			CodeMiningAnnotation ann= (CodeMiningAnnotation) annotation;
			Position p= annotationModel.getPosition(annotation);
			if (p != null && p.offset == pos.offset) {
				return ann;
			}
		}
		return null;
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock= ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Remove the codemining annotations.
	 */
	private void removeCodeMiningAnnotations() {

		IAnnotationModel annotationModel= fViewer.getAnnotationModel();
		if (annotationModel == null || fCodeMiningAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						fCodeMiningAnnotations.toArray(new Annotation[fCodeMiningAnnotations.size()]), null);
			} else {
				for (Annotation fColorSymbolAnnotation : fCodeMiningAnnotations)
					annotationModel.removeAnnotation(fColorSymbolAnnotation);
			}
			fCodeMiningAnnotations= null;
		}
	}

	/**
	 * Returns the line spacing from the given line index with the codemining annotations height and
	 * null otherwise.
	 */
	@Override
	public Integer getLineSpacing(int lineIndex) {
		CodeMiningAnnotation annotation= CodeMiningUtilities.getCodeMiningAnnotationAtLine(fViewer, lineIndex);
		return annotation != null ? annotation.getHeight() : null;
	}

}
