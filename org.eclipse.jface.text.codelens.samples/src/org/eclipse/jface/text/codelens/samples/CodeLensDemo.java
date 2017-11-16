package org.eclipse.jface.text.codelens.samples;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.codelens.CodeLensManager;
import org.eclipse.jface.text.codelens.ICodeLensProvider;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CodeLensDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("CodeLens demo");

		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document("class A"), new AnnotationModel());

		AnnotationPainter painter = createAnnotationPainter(sourceViewer);
		CodeLensManager manager = new CodeLensManager();
		manager.install(sourceViewer, painter, new ICodeLensProvider[] { new ClassReferencesCodeLensProvider(),
				new ClassImplementationsCodeLensProvider() });
		manager.run();

		sourceViewer.getTextWidget().addModifyListener(e -> {
			manager.run();
		});

		// shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		manager.uninstall();
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

}
