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
package org.eclipse.jface.text.examples.codemining;

import org.eclipse.jface.internal.text.codemining.CodeMiningManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
//import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Code Mining demo with class references, implementations.
 *
 */
public class CodeMiningDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Code Mining demo");

		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(
				new Document("// Type class & new keyword and see references CodeMining\n"
						+ "// Name class with a number N to emulate Nms before resolving the references CodeMining \n\n"
						+ "class A\n" + "new A\n" + "new A\n\n" + "class 5\n" + "new 5\n" + "new 5\n" + "new 5"),
				new AnnotationModel());
		// Add AnnotationPainter (required by CodeMining)
		AnnotationPainter painter = createAnnotationPainter(sourceViewer);
		// Install Inlined annotation support
		InlinedAnnotationSupport support = new InlinedAnnotationSupport();
		support.install(sourceViewer, painter);

		// Create manager
		CodeMiningManager manager = new CodeMiningManager(sourceViewer, support, new ICodeMiningProvider[] { new ClassReferenceCodeMiningProvider(),
				new ClassImplementationsCodeMiningProvider() });

		// Execute manager in a reconciler
		MonoReconciler reconciler = new MonoReconciler(new IReconcilingStrategy() {

			@Override
			public void setDocument(IDocument document) {
				manager.run();
			}

			@Override
			public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {

			}

			@Override
			public void reconcile(IRegion partition) {
				manager.run();
			}
		}, false);
		//reconciler.setDelay(1);
		reconciler.install(sourceViewer);

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
			@Override
			public Object getType(Annotation annotation) {
				return annotation.getType();
			}

			@Override
			public boolean isMultiLine(Annotation annotation) {
				return true;
			}

			@Override
			public boolean isTemporary(Annotation annotation) {
				return true;
			}

		};
		AnnotationPainter painter = new AnnotationPainter(viewer, annotationAccess);
		((ITextViewerExtension2) viewer).addPainter(painter);
		return painter;
	}

}
