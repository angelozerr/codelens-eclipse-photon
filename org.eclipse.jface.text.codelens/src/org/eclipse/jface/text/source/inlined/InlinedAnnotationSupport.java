package org.eclipse.jface.text.source.inlined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
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

public class InlinedAnnotationSupport implements StyledTextLineSpacingProvider {

	/**
	 * The annotation codemining strategy singleton.
	 */
	private static final IDrawingStrategy INLINED_STRATEGY = new InlinedDrawingStrategy();

	/**
	 * The annotation codemining strategy ID.
	 */
	private static final String INLINED_STRATEGY_ID = "inlined"; //$NON-NLS-1$

	/**
	 * The source viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * The annotation painter to use to draw the inlined annotations.
	 */
	private AnnotationPainter fPainter;

	/**
	 * The font to use to draw the code minings annotations.
	 */
	private Font fFont;

	private Set<InlinedAnnotation> fInlinedAnnotations;

	public void install(ISourceViewer viewer, AnnotationPainter painter) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(painter);
		fViewer = viewer;
		fPainter = painter;
		initPainter();
		StyledText text = fViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		// Initialize defaut code mining font and color.
		FontData[] fds = text.getFont().getFontData();
		for (int i = 0; i < fds.length; i++) {
			fds[i].setStyle(fds[i].getStyle() | SWT.ITALIC);
		}
		setFont(new Font(text.getDisplay(), fds));
		setColor(text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		text.setLineSpacingProvider(this);
	}

	/**
	 * Initialize painter with code mining drawing strategy.
	 * 
	 */
	private void initPainter() {
		fPainter.addDrawingStrategy(INLINED_STRATEGY_ID, INLINED_STRATEGY);
		fPainter.addAnnotationType(InlinedAnnotation.TYPE, INLINED_STRATEGY_ID);
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
	 * Set the color to use to draw the code minings annotations.
	 * 
	 * @param color
	 *            the color to use to draw the code minings annotations.
	 */
	public void setColor(Color color) {
		fPainter.setAnnotationTypeColor(InlinedAnnotation.TYPE, color);
	}

	public void uninstall() {
		fViewer = null;
		fPainter = null;
	}

	public void updateAnnotations(Set<InlinedAnnotation> annotations) {
		IDocument document = fViewer != null ? fViewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before rendered is done.
			return;
		}
		IAnnotationModel annotationModel = fViewer.getAnnotationModel();
		if (annotationModel == null) {
			return;
		}
		Map<InlinedAnnotation, Position> annotationsToAdd = new HashMap<>();
		List<InlinedAnnotation> annotationsToRemove = fInlinedAnnotations != null ? new ArrayList<>(fInlinedAnnotations)
				: Collections.emptyList();
		for (InlinedAnnotation ann : annotations) {
			if (!annotationsToRemove.remove(ann)) {
				annotationsToAdd.put(ann, ann.getPosition());
			}
		}
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
				// "Enter"), but we don't need to redraw the viewer because change of position
				// is done by AnnotationPainter.
			} else {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(
							annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]), annotationsToAdd);
				} else {
					removeInlinedAnnotations();
					Iterator<Entry<InlinedAnnotation, Position>> iter = annotationsToAdd.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<InlinedAnnotation, Position> mapEntry = iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
			fInlinedAnnotations = annotations;
		}
	}

	/**
	 * Returns the existing codemining annotation with the given position
	 * information and null otherwise.
	 * 
	 * @param pos
	 *            the position
	 * @return the existing codemining annotation with the given position
	 *         information and null otherwise.
	 */
	public InlinedAnnotation findExistingAnnotation(Position pos) {
		if (fInlinedAnnotations == null) {
			return null;
		}
		for (InlinedAnnotation ann : fInlinedAnnotations) {
			if (ann.getPosition().offset == pos.offset) {
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
	 * Remove the codemining annotations.
	 */
	private void removeInlinedAnnotations() {

		IAnnotationModel annotationModel = fViewer.getAnnotationModel();
		if (annotationModel == null || fInlinedAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						fInlinedAnnotations.toArray(new Annotation[fInlinedAnnotations.size()]), null);
			} else {
				for (InlinedAnnotation annotation : fInlinedAnnotations)
					annotationModel.removeAnnotation(annotation);
			}
			fInlinedAnnotations = null;
		}
	}

	/**
	 * Returns the line spacing from the given line index with the codemining
	 * annotations height and null otherwise.
	 */
	@Override
	public Integer getLineSpacing(int lineIndex) {
		InlinedAnnotation annotation = getInlinedAnnotationAtLine(fViewer, lineIndex);
		return annotation != null && annotation.isShowAtBeforeLine() ? annotation.getHeight() : null;
	}

	/**
	 * Returns the {@link InlinedAnnotation} from the given line index and null
	 * otherwise.
	 * 
	 * @param viewer
	 *            the source viewer
	 * @param lineIndex
	 *            the line index.
	 * @return the {@link InlinedAnnotation} from the given line index and null
	 *         otherwise.
	 */
	public static InlinedAnnotation getInlinedAnnotationAtLine(ISourceViewer viewer, int lineIndex) {
		if (viewer == null) {
			return null;
		}
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null) {
			return null;
		}
		IDocument document = viewer.getDocument();
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
				if (ann instanceof InlinedAnnotation) {
					Position p = annotationModel.getPosition(ann);
					if (p != null) {
						if (p.overlapsWith(line.getOffset(), line.getLength())) {
							return (InlinedAnnotation) ann;
						}
					}
				}
			}
		} catch (BadLocationException e) {
			return null;
		}
		return null;
	}
}
