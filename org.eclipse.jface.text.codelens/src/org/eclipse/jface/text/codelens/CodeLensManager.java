/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeLens support - Bug XXXXXX
 */
package org.eclipse.jface.text.codelens;

import java.lang.reflect.Field;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * CodeLens manager.
 *
 */
public class CodeLensManager implements Runnable, StyledTextLineSpacingProvider {

	private static final IDrawingStrategy CODELENS_STRATEGY = new CodeLensDrawingStrategy();
	private static final Object CODELENS = "codelens";
	private static final Color DUMMY_COLOR = new Color(null, new RGB(0, 0, 0));

	/**
	 * Holds the current color symbol annotations.
	 */
	private List<CodeLensAnnotation> codeLensAnnotations = null;

	private ISourceViewer viewer;
	private AnnotationPainter painter;
	private List<ICodeLensProvider> codeLensProviders;
	private IProgressMonitor monitor;
	private CompletableFuture<List<? extends ICodeLens>> codeLensProviderRequest;
	private List<CompletableFuture<ICodeLens>> codeLensResolverRequest;

	public void install(ISourceViewer viewer, ICodeLensProvider[] codeLensProviders) {
		Assert.isNotNull(viewer);
		this.viewer = viewer;
		this.painter = null;
		setCodeLensProviders(codeLensProviders);
		StyledText text = this.viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		text.setLineSpacingProvider(this);
	}

	/**
	 * Uninstalls this codelens manager.
	 */
	public void uninstall() {
		StyledText text = viewer.getTextWidget();
		if (text != null && !text.isDisposed()) {

		}
	}

	public void setCodeLensProviders(ICodeLensProvider[] codeLensProviders) {
		this.codeLensProviders = Arrays.asList(codeLensProviders);
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Collect, resolve and render the lenses of the viewer.
	 */
	@Override
	public void run() {
		if (viewer == null || codeLensProviders == null || viewer.getAnnotationModel() == null) {
			return;
		}
		initPainterIfNeeded();
		cancel();
		monitor = new CodeLensMonitor();
		final IProgressMonitor m = monitor;
		// Collect the lenses for the viewer
		codeLensProviderRequest = getCodeLenses(viewer, codeLensProviders, m);
		codeLensProviderRequest.thenAccept(symbols -> {
			// check if request was canceled.
			m.isCanceled();
			// then group lenses by lines position
			Map<Position, List<ICodeLens>> groups = goupByLines(symbols, codeLensProviders);
			// resolve and render lenses
			codeLensResolverRequest = renderCodeLenses(groups, viewer, m);
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
								((CodeLensAnnotation) ann).redraw(viewer.getTextWidget());
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
		// Cancel the last request which provides the lenses.
		cancel(codeLensProviderRequest);
		// Cancel the last request which resolves the lenses.
		if (codeLensResolverRequest != null) {
			codeLensResolverRequest.stream().forEach(f -> cancel(f));
		}

	}

	private static void cancel(CompletableFuture<?> f) {
		if (f != null && !f.isCancelled() && !f.isDone()) {
			f.cancel(true);
		}
	}

	/**
	 * Dispose the codelens manager.
	 */
	public void dispose() {
		cancel();
	}

	// --------------- CodeLens providers methods utilities

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

	// --------------- CodeLens renderer methods utilities

	/**
	 * 
	 * @param groups
	 * @param viewer
	 * @param monitor
	 * @return
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
		// Collect lenses to resolve
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
				ann = new CodeLensAnnotation();
				annotationsToAdd.put(ann, pos);
			} else {
				// The annotation exists, remove it from the list to delete.
				annotationsToRemove.remove(ann);
			}
			ann.update(lenses);
			for (ICodeLens lens : lenses) {
				if (!lens.isResolved() && lens.getResolver() != null) {
					CompletableFuture<ICodeLens> promise = lens.getResolver().resolveCodeLens(viewer, lens, monitor);
					if (promise.getNow(null) == null) {
						lensesToResolve.add(promise);
					}
				}
			}

			currentAnnotations.add(ann);
		});
		// Mark annotations as deleted to redraw the styled text when annotation must be
		// drawn.
		for (Annotation ann : annotationsToRemove) {
			ann.markDeleted(true);
		}
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
					removeColorSymbolAnnotations();
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
	 * Returns existing codelens annotation with the given position information and
	 * null otherwise.
	 * 
	 * @param pos
	 * @param annotationModel
	 * @return
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

	private static CodeLensAnnotation getCodeLensAnnotationAtLine(IAnnotationModel annotationModel, IDocument document,
			int lineIndex) {
		int lineNumber = lineIndex + 1;
		if (lineNumber > document.getNumberOfLines()) {
			return null;
		}
		try {
			IRegion line = document.getLineInformation(lineNumber);
			Iterator<Annotation> iter = (annotationModel instanceof IAnnotationModelExtension2)
					? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(line.getOffset(),
							line.getLength(), true, true)
					: annotationModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation ann = iter.next();
				if (ann instanceof CodeLensAnnotation) {
					Position p = annotationModel.getPosition(ann);
					if (p != null) {
						if (p.overlapsWith(line.getOffset(), line.getLength())) {
							return (CodeLensAnnotation) ann;
						}
					}
				}
			}
		} catch (BadLocationException e) {
		}
		return null;
	}

	@Override
	public Integer getLineSpacing(int lineIndex) {
		if (viewer == null) {
			return null;
		}
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null) {
			return null;
		}
		IDocument document = viewer.getDocument();
		CodeLensAnnotation annotation = getCodeLensAnnotationAtLine(annotationModel, document, lineIndex);
		return annotation != null ? annotation.getHeight() : null;
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
	 * 
	 */
	private void removeColorSymbolAnnotations() {

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

	// Painter initialisation utilities

	private AnnotationPainter initPainterIfNeeded() {
		if (painter != null) {
			return painter;
		}
		return getAndInitAnnotationPainter();
	}

	private synchronized AnnotationPainter getAndInitAnnotationPainter() {
		if (painter != null) {
			return painter;
		}
		AnnotationPainter painter = getExistingAnnotationPainter(viewer);
		if (painter == null) {
			painter = createPainter(viewer);
			((ITextViewerExtension2) viewer).addPainter(painter);
		}
		painter.addDrawingStrategy(CODELENS, CODELENS_STRATEGY);
		painter.addAnnotationType(CodeLensAnnotation.TYPE, CODELENS);
		// the painter needs a color for an annotation type
		// we must set it with a dummy color even if we don't use it to draw the color
		// symbol.
		painter.setAnnotationTypeColor(CodeLensAnnotation.TYPE, DUMMY_COLOR);
		this.painter = painter;
		return painter;
	}

	protected AnnotationPainter createPainter(ISourceViewer textViewer) {
		IAnnotationAccess annotationAccess = new IAnnotationAccess() {
			public Object getType(Annotation annotation) {
				return annotation.getType();
			}

			public boolean isMultiLine(Annotation annotation) {
				return true;
			}

			public boolean isTemporary(Annotation annotation) {
				return true;
			}

		};
		return new AnnotationPainter(textViewer, annotationAccess);
	}

	/**
	 * Retrieve the annotation painter used by the given text viewer.
	 * 
	 * @param viewer
	 * @return
	 */
	private static AnnotationPainter getExistingAnnotationPainter(ITextViewer viewer) {
		// Here reflection is used, because
		// - it doesn't exists API public for get AnnotationPainter used by the viewer.
		// - it doesn't exists extension point to register custom drawing strategy. See
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=51498
		PaintManager paintManager = getFieldValue(viewer, "fPaintManager", TextViewer.class);
		if (paintManager != null) {
			List<IPainter> painters = getFieldValue(paintManager, "fPainters", PaintManager.class);
			if (painters != null) {
				for (IPainter painter : painters) {
					if (painter instanceof AnnotationPainter) {
						return (AnnotationPainter) painter;
					}
				}
			}
		}
		return null;
	}

	private static <T> T getFieldValue(Object object, String name, Class clazz) {
		Field f = getDeclaredField(clazz, name);
		if (f != null) {
			try {
				return (T) f.get(object);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private static Field getDeclaredField(Class clazz, String name) {
		if (clazz == null) {
			return null;
		}
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (NoSuchFieldException e) {
			return getDeclaredField(clazz.getSuperclass(), name);
		} catch (SecurityException e) {
			return null;
		}
	}

}
