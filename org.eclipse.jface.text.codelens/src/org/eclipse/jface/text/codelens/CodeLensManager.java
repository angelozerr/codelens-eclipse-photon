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
	private CompletableFuture<List<? extends ICodeLens>> codeLensRequest;
	private List<CompletableFuture<ICodeLens>> promises;
	private CompletableFuture<List<ICodeLens>> h;

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
		codeLensRequest = getCodeLenses(viewer, codeLensProviders, m);
		codeLensRequest.thenAccept(symbols -> {
			// then group lenses by lines position
			Map<Position, List<ICodeLens>> groups = goupByLines(symbols, codeLensProviders);
			// resolve and render lenses
			promises = renderCodeLenses(groups, viewer, m);
			if (promises != null) {
				for (CompletableFuture<ICodeLens> p : promises) {
					if (!p.isCancelled()) {
						p.thenAccept(lens -> {
							if (m.isCanceled()) {
								return;
							}
							Position pos = lens.getPosition();
							IAnnotationModel annotationModel = viewer.getAnnotationModel();
							Iterator<Annotation> iter = (annotationModel instanceof IAnnotationModelExtension2)
									? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(pos.offset,
											1, true, true)
									: annotationModel.getAnnotationIterator();
							while (iter.hasNext()) {
								Annotation ann = iter.next();
								if (ann instanceof CodeLensAnnotation) {
									if (m.isCanceled()) {
										return;
									}
									viewer.getTextWidget().getDisplay()
											.asyncExec(() -> CodeLensDrawingStrategy.draw((CodeLensAnnotation) ann,
													null, viewer.getTextWidget(), pos.offset, 1, null));
								}
							}
						});
					} else {
						System.err.println("resolve canceled");
					}
				}
			}

		});
	}

	/**
	 * Cancel the last request which collect the lenses.
	 */
	private void cancel() {
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		if (codeLensRequest != null && !codeLensRequest.isDone()) {
			codeLensRequest.cancel(true);
		}
		if (promises != null) {
			promises.forEach(p -> {
				if (!p.isDone()) {
					p.cancel(true);
				}
			});
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
	 * Return a list of {@link CompletableFuture} which provides the list of
	 * {@link ICodeLens} for the given <code>viewer</code> by using the given
	 * providers.
	 * 
	 * @param viewer
	 * @param monitor
	 * @param providers
	 * @return
	 */
	private static CompletableFuture<List<? extends ICodeLens>> getCodeLenses(ITextViewer viewer,
			List<ICodeLensProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeLens>>> com = providers.stream()
				.map(provider -> provider.provideCodeLenses(viewer, monitor)).collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
	}

	/**
	 * 
	 * @param lenses
	 * @param providers
	 */
	private static Map<Position, List<ICodeLens>> goupByLines(List<? extends ICodeLens> lenses,
			List<ICodeLensProvider> providers) {
		// sort lenses by lineNumber and provider-rank if
		Collections.sort(lenses, (a, b) -> {
			if (a.getPosition().offset < b.getPosition().offset) {
				return -1;
			} else if (a.getPosition().offset > b.getPosition().offset) {
				return 1;
			} else if (providers.indexOf(a.getResolver()) < providers.indexOf(b.getResolver())) {
				return -1;
			} else if (providers.indexOf(a.getResolver()) > providers.indexOf(b.getResolver())) {
				return 1;
			}
			/*
			 * else if (a.getSymbol().getRange().startColumn <
			 * b.getSymbol().getRange().startColumn) { return -1; } else if
			 * (a.getSymbol().getRange().startColumn > b.getSymbol().getRange().startColumn)
			 * { return 1; }
			 */ else {
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
		if (monitor.isCanceled()) {
			return null;
		}
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
				ann = new CodeLensAnnotation();
				annotationsToAdd.put(ann, pos);
			} else {
				// The annotation exists, remove it from the list to delete.
				annotationsToRemove.remove(ann);
			}
			ann.setCodeLenses(lenses, viewer, monitor);
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
		synchronized (getLockObject(annotationModel)) {

			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of codelens
				// range
				// (ex: user key press
				// "Enter"), but we don't need to redraw the viewer because change of position
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
		for (Annotation ann : annotationsToRemove) {
			((CodeLensAnnotation) ann).dispose();
		}
		return lensesToResolve;
		// for (CodeLensAnnotation annotation : currentAnnotations) {
		//
		// final Position pos = annotationModel.getPosition(annotation);
		// resolveCodeLens(viewer, annotation.getLenses(), monitor)
		// .thenAccept(lenses -> viewer.getTextWidget().getDisplay().asyncExec(() -> {
		// if (monitor.isCanceled()) {
		// return;
		// }
		// System.err.println("draw->" + lenses.get(0).getPosition().offset);
		//
		// CodeLensDrawingStrategy.draw(annotation, null, viewer.getTextWidget(),
		// pos.offset, 1, null);
		//
		// }));
		// }

	}

	private static CompletableFuture<List<ICodeLens>> resolveCodeLens(ITextViewer viewer, List<ICodeLens> lenses,
			IProgressMonitor monitor) {
		List<CompletableFuture<ICodeLens>> com = lenses.stream()
				.map(lens -> lens.getResolver().resolveCodeLens(viewer, lens, monitor)).collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()]))
				.thenApply(v -> com.stream().map(CompletableFuture::join).collect(Collectors.toList()));
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
					? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(line.getOffset(), 1, true,
							true)
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
		// System.err.println(lineIndex + "-> " + annotation);
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
