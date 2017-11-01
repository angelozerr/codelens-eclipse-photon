package org.eclipse.jface.text.codelens;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.IAnnotationAccess;
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
	 * The type of codelens annotations.
	 */
	public static final String TYPE = "org.eclipse.jface.text.codelens"; //$NON-NLS-1$

	private ISourceViewer viewer;
	private AnnotationPainter painter;
	private ICodeLensProvider[] codeLensProviders;
	private IProgressMonitor monitor;

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
		AnnotationPainter painter = getAnnotationPainter();
		for (int i = 0; i < codeLensProviders.length; i++) {
			ICodeLensProvider provider = codeLensProviders[i];
			provider.provideCodeLenses(viewer, monitor).thenApply(lenses -> {
				for (ICodeLens codeLens : lenses) {
					try {
						provider.resolveCodeLens(viewer, codeLens, monitor).get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return lenses;
			}).thenAccept(lenses -> {
				for (ICodeLens codeLens : lenses) {
					Position p;
					try {
						p = codeLens.getPosition(viewer.getDocument());
						if (p != null)
							viewer.getAnnotationModel().addAnnotation(new Annotation(TYPE, true, codeLens.getCommand().getTitle()), p);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
		}
	}

	private AnnotationPainter getAnnotationPainter() {
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
		painter.addAnnotationType(TYPE, CODELENS);
		// the painter needs a color for an annotation type
		// we must set it with a dummy color even if we don't use it to draw the color
		// symbol.
		painter.setAnnotationTypeColor(TYPE, DUMMY_COLOR);
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
		return null;
	}
}
