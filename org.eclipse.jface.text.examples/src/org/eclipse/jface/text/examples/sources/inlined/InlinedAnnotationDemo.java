/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.examples.sources.inlined;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.InlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.source.inlined.Positions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * An inlined demo with block and inline annotations.
 *
 */
public class InlinedAnnotationDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Inlined annotation demo");

		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document("\ncolor:rgb(1,1,1)\ncolor: rgb(1,"), new AnnotationModel());
		sourceViewer.setDocument(new Document("\ncolor:rgb(255, 255, 0)"), new AnnotationModel());
		// Add AnnotationPainter (required by InlinedAnnotation)
		InlinedAnnotationSupport support = new InlinedAnnotationSupport();
		support.install(sourceViewer, createAnnotationPainter(sourceViewer));

		Set<InlinedAnnotation> annotations = getInlinedAnnotation(sourceViewer, support);
		support.updateAnnotations(annotations);

		sourceViewer.getTextWidget().addModifyListener(e -> {
			Set<InlinedAnnotation> anns = getInlinedAnnotation(sourceViewer, support);
			support.updateAnnotations(anns);
		});

		// shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static AnnotationPainter createAnnotationPainter(ISourceViewer viewer) {
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
		AnnotationPainter painter = new AnnotationPainter(viewer, annotationAccess);
		((ITextViewerExtension2) viewer).addPainter(painter);
		return painter;
	}

	private static Set<InlinedAnnotation> getInlinedAnnotation(ISourceViewer viewer, InlinedAnnotationSupport support) {
		IDocument document = viewer.getDocument();
		Set<InlinedAnnotation> annotations = new HashSet<>();
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false).trim();
			int index = line.indexOf("color:");
			if (index == 0) {
				String rgb = line.substring(index + "color:".length(), line.length()).trim();
				try {
					String status = "OK!";
					Color color = parse(rgb, viewer.getTextWidget().getDisplay());
					if (color != null) {
					} else {
						status = "ERROR!";
					}
					// Status color annotation
					Position pos = Positions.of(i, document, true);
					ColorStatusAnnotation statusAnnotation = support.findExistingAnnotation(pos);
					if (statusAnnotation == null) {
						statusAnnotation = new ColorStatusAnnotation(pos, viewer.getTextWidget());
					}
					statusAnnotation.setStatus(status);
					annotations.add(statusAnnotation);

					// Color annotation
					if (color != null) {
						Position colorPos = new Position(pos.offset + index + "color:".length(), rgb.length());
						ColorAnnotation colorAnnotation = support.findExistingAnnotation(colorPos);
						if (colorAnnotation == null) {
							colorAnnotation = new ColorAnnotation(colorPos, viewer.getTextWidget());
						}
						colorAnnotation.setColor(color);
						annotations.add(colorAnnotation);
					}

				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		return annotations;
	}

	private static Color parse(String input, Device device) {
		Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
		Matcher m = c.matcher(input);

		if (m.matches()) {
			try {
				return new Color(device, Integer.valueOf(m.group(1)), // r
						Integer.valueOf(m.group(2)), // g
						Integer.valueOf(m.group(3))); // b
			} catch (Exception e) {

			}
		}
		return null;
	}

	private static String getLineText(IDocument document, int line, boolean withLineDelimiter) {
		try {
			int lo = document.getLineOffset(line);
			int ll = document.getLineLength(line);
			if (!withLineDelimiter) {
				String delim = document.getLineDelimiter(line);
				ll = ll - (delim != null ? delim.length() : 0);
			}
			return document.get(lo, ll);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
