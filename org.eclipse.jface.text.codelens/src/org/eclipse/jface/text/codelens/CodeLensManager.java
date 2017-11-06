package org.eclipse.jface.text.codelens;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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

public class CodeLensManager implements StyledTextLineSpacingProvider {

	private static final IDrawingStrategy CODELENS_STRATEGY = new CodeLensDrawingStrategy();
	private static final Object CODELENS = "codelens";
	private static final Color DUMMY_COLOR = new Color(null, new RGB(0, 0, 0));

	/**
	 * Holds the current color symbol annotations.
	 */
	private List<Annotation> colorSymbolAnnotations = null;

	private ISourceViewer viewer;
	private AnnotationPainter painter;
	private ICodeLensProvider[] codeLensProviders;
	private IProgressMonitor monitor;
	private CompletableFuture<List<? extends ICodeLens>> codeLensRequest;

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
		this.codeLensProviders = codeLensProviders;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void refresh() {
		if (viewer == null || codeLensProviders == null || viewer.getAnnotationModel() == null) {
			return;
		}
		initPainterIfNeeded();
		if (codeLensRequest != null && !codeLensRequest.isDone()) {
			codeLensRequest.cancel(true);
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		codeLensRequest = getCodeLenses(viewer, monitor, codeLensProviders);
		codeLensRequest.thenAccept(symbols -> {
			/*
			 * Collections.sort(symbols, (a, b) -> { // sort by lineNumber, provider-rank,
			 * and column if (a.startLineNumber < b.getSymbol().getRange().startLineNumber)
			 * { return -1; } else if (a.getSymbol().getRange().startLineNumber >
			 * b.getSymbol().getRange().startLineNumber) { return 1; } else if
			 * (providers.indexOf(a.getProvider()) < providers.indexOf(b.getProvider())) {
			 * return -1; } else if (providers.indexOf(a.getProvider()) >
			 * providers.indexOf(b.getProvider())) { return 1; } else if
			 * (a.getSymbol().getRange().startColumn < b.getSymbol().getRange().startColumn)
			 * { return -1; } else if (a.getSymbol().getRange().startColumn >
			 * b.getSymbol().getRange().startColumn) { return 1; } else { return 0; } });
			 */
			System.err.println(symbols);
			renderCodeLenses(symbols, viewer, monitor);
		});
		// for (int i = 0; i < codeLensProviders.length; i++) {
		// ICodeLensProvider provider = codeLensProviders[i];
		// provider.provideCodeLenses(viewer, monitor).thenApply(lenses -> {
		// for (ICodeLens codeLens : lenses) {
		// try {
		// provider.resolveCodeLens(viewer, codeLens, monitor).get();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// return lenses;
		// }).thenAccept(lenses -> {
		// for (ICodeLens codeLens : lenses) {
		// Position p;
		// try {
		// p = codeLens.getPosition(viewer.getDocument());
		// if (p != null)
		// viewer.getAnnotationModel()
		// .addAnnotation(new Annotation(TYPE, true, codeLens.getCommand().getTitle()),
		// p);
		// } catch (BadLocationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// });
		// }
	}

	// private static CompletableFuture<Collection<CodeLensData>>
	// getCodeLensData(ITextViewer viewer,
	// List<ICodeLensProvider> providers, IProgressMonitor monitor) {
	// List<CompletableFuture<List<? extends ICodeLens>>> providers = new
	// ArrayList<>();
	// for (int i = 0; i < codeLensProviders.length; i++) {
	// providers.add(codeLensProviders[i].provideCodeLenses(viewer, monitor));
	// }
	// return sequence(providers).thenAccept(all -> {
	// List<CodeLensData> symbols = new ArrayList<>();
	// for (List<? extends ICodeLens> lenses : all) {
	//
	// }
	//
	// Collections.sort(symbols, (a, b) -> {
	// // sort by lineNumber, provider-rank, and column
	// if (a.getSymbol().getRange().startLineNumber <
	// b.getSymbol().getRange().startLineNumber) {
	// return -1;
	// } else if (a.getSymbol().getRange().startLineNumber >
	// b.getSymbol().getRange().startLineNumber) {
	// return 1;
	// } else if (providers.indexOf(a.getProvider()) <
	// providers.indexOf(b.getProvider())) {
	// return -1;
	// } else if (providers.indexOf(a.getProvider()) >
	// providers.indexOf(b.getProvider())) {
	// return 1;
	// } else if (a.getSymbol().getRange().startColumn <
	// b.getSymbol().getRange().startColumn) {
	// return -1;
	// } else if (a.getSymbol().getRange().startColumn >
	// b.getSymbol().getRange().startColumn) {
	// return 1;
	// } else {
	// return 0;
	// }
	// });
	// return symbols;
	// });
	// }

	static CompletableFuture<List<? extends ICodeLens>> getCodeLenses(ITextViewer viewer, IProgressMonitor monitor,
			ICodeLensProvider[] providers) {
		List<CompletableFuture<List<? extends ICodeLens>>> com = Stream.of(providers)
				.map(provider -> provider.provideCodeLenses(viewer, monitor))
				// .map(lenses -> new CodeLensData(symbol, com))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
		/*
		 * .sorted((a, b) -> { // sort by lineNumber, provider-rank, and column if
		 * (a.getSymbol().getRange().startLineNumber <
		 * b.getSymbol().getRange().startLineNumber) { return -1; } else if
		 * (a.getSymbol().getRange().startLineNumber >
		 * b.getSymbol().getRange().startLineNumber) { return 1; } else if
		 * (providers.indexOf(a.getProvider()) < providers.indexOf(b.getProvider())) {
		 * return -1; } else if (providers.indexOf(a.getProvider()) >
		 * providers.indexOf(b.getProvider())) { return 1; } else if
		 * (a.getSymbol().getRange().startColumn < b.getSymbol().getRange().startColumn)
		 * { return -1; } else if (a.getSymbol().getRange().startColumn >
		 * b.getSymbol().getRange().startColumn) { return 1; } else { return 0; } })
		 */
		// .collect(Collectors.toList());
	}

	private void renderCodeLenses(List<? extends ICodeLens> symbols, ISourceViewer viewer, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}
		IDocument document = viewer != null ? viewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before codelens rendered is done.
			return;
		}
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		Map<Annotation, Position> annotationsToAdd = new HashMap<>();
		// Initialize annotations to delete with last annotations
		List<Annotation> annotationsToRemove = colorSymbolAnnotations != null ? new ArrayList<>(colorSymbolAnnotations)
				: Collections.emptyList();
		List<Annotation> currentAnnotations = new ArrayList<>();
		// Loop for codelens
		for (ICodeLens codeLens : symbols) {
			try {
				codeLens.getProvider().resolveCodeLens(viewer, codeLens, monitor).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Position pos = codeLens.getPosition();
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
			ann.addCodeLens(codeLens);
			currentAnnotations.add(ann);
		}

		synchronized (getLockObject(annotationModel)) {
			colorSymbolAnnotations = currentAnnotations;
			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of color range
				// (ex: user key press
				// "Enter"), but we don't need to redraw the viewer because change of position
				// is done by AnnotationPainter.
				return;
			}
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
		int maxLineNumber = document.getNumberOfLines();
		List<List<ICodeLens>> groups = new ArrayList<>();
		List<ICodeLens> lastGroup = null;

		// for (ICodeLens symbol : symbols) {
		// int line = symbol.getSymbol().getRange().startLineNumber;
		// if (line < 1 || line > maxLineNumber) {
		// // invalid code lens
		// continue;
		// } else if (lastGroup != null
		// && lastGroup.get(lastGroup.size() - 1).getSymbol().getRange().startLineNumber
		// == line) {
		// // on same line as previous
		// lastGroup.add(symbol);
		// } else {
		// // on later line as previous
		// lastGroup = new ArrayList<>(Arrays.asList(symbol));
		// groups.add(lastGroup);
		// }
		// }
		//
		// int codeLensIndex = 0, groupsIndex = 0;
		// CodeLensHelper helper = new CodeLensHelper();
		//
		// while (groupsIndex < groups.size() && codeLensIndex < this._lenses.size()) {
		//
		// int symbolsLineNumber =
		// groups.get(groupsIndex).get(0).getSymbol().getRange().startLineNumber;
		// int offset = this._lenses.get(codeLensIndex).getOffsetAtLine();
		// int codeLensLineNumber = -1;
		// try {
		// codeLensLineNumber = offset != -1 ? document.getLineOfOffset(offset) + 1 :
		// -1;
		// } catch (BadLocationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } // this._lenses.get(codeLensIndex).getLineNumber();
		//
		// if (codeLensLineNumber < symbolsLineNumber) {
		// this._lenses.get(codeLensIndex).dispose(helper, accessor);
		// this._lenses.remove(codeLensIndex);// .splice(codeLensIndex,
		// // 1);
		// } else if (codeLensLineNumber == symbolsLineNumber) {
		// this._lenses.get(codeLensIndex).updateCodeLensSymbols(groups.get(groupsIndex),
		// helper);
		// groupsIndex++;
		// codeLensIndex++;
		// } else {
		// this._lenses.add(codeLensIndex, new CodeLens(groups.get(groupsIndex), helper,
		// accessor));
		// // this._lenses.splice(codeLensIndex, 0, new
		// // CodeLens(groups.get(groupsIndex),
		// // /* this._editor, */ helper, accessor
		// /*
		// * , this._commandService, this._messageService, () =>
		// * this._detectVisibleLenses.schedule() //));
		// */
		// codeLensIndex++;
		// groupsIndex++;
		// }
		// }
		// // Delete extra code lenses
		// while (codeLensIndex < this._lenses.size()) {
		// this._lenses.get(codeLensIndex).dispose(helper, accessor);
		// this._lenses.remove(codeLensIndex);// splice(codeLensIndex, 1);
		// }
		//
		// // Create extra symbols
		// while (groupsIndex < groups.size()) {
		// this._lenses.add(new CodeLens(groups.get(groupsIndex) /* this._editor */,
		// helper,
		// accessor/*
		// * , this._commandService, this._messageService, () =>
		// * this._detectVisibleLenses.schedule())
		// */));
		// groupsIndex++;
		// }

		// helper.commit(changeAccessor);
		// Display.getDefault().asyncExec(() -> {
		//// this._lenses.forEach((lens) -> {
		//// lens.redraw(accessor);
		//// });
		// textViewer.getTextWidget().redraw();
		// });
		// _onViewportChanged();
		// viewer.getTextWidget().getDisplay().asyncExec(() -> {
		// viewer.getTextWidget().redraw();
		// });
		// viewer.invalidateTextPresentation();
	}

	/**
	 * Returns existing codelens annotation with the given position and rgb color
	 * information and null otherwise.
	 * 
	 * @param pos
	 * @param annotationModel
	 * @param rgba
	 * @return
	 */
	private CodeLensAnnotation findExistingAnnotation(Position pos, IAnnotationModel annotationModel) {
		if (colorSymbolAnnotations == null) {
			return null;
		}
		for (Annotation annotation : colorSymbolAnnotations) {
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

	void removeColorSymbolAnnotations() {

		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null || colorSymbolAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						colorSymbolAnnotations.toArray(new Annotation[colorSymbolAnnotations.size()]), null);
			} else {
				for (Annotation fColorSymbolAnnotation : colorSymbolAnnotations)
					annotationModel.removeAnnotation(fColorSymbolAnnotation);
			}
			colorSymbolAnnotations = null;
		}
	}

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
		if (getCodeLensAnnotationAtLine(annotationModel, document, lineIndex) != null) {
			return 20;
		}
		return null;
	}

	private static CodeLensAnnotation getCodeLensAnnotationAtLine(IAnnotationModel annotationModel, IDocument document,
			int lineIndex) {
		int lineNumber = lineIndex;
		if (lineNumber >= document.getNumberOfLines()) {
			return null;
		}
		try {
			IRegion line = document.getLineInformation(lineNumber);
			System.err.println(document.get(line.getOffset(), line.getLength()));
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
}
