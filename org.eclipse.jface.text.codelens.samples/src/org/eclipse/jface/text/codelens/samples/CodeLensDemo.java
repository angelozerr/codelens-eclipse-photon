package org.eclipse.jface.text.codelens.samples;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.codelens.CodeLensManager;
import org.eclipse.jface.text.codelens.ICodeLensProvider;
import org.eclipse.jface.text.source.AnnotationModel;
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
		String delim = sourceViewer.getTextWidget().getLineDelimiter();
		sourceViewer.setDocument(new Document(delim + "  class A" + delim + "new A" + delim + "new A" + delim + "class B"
				+ delim + "new B" + delim + "interface I" + delim + "class C implements I"), new AnnotationModel());

		//sourceViewer.setDocument(new Document(delim + "class A"), new AnnotationModel());
		
		CodeLensManager manager = new CodeLensManager();
		manager.install(sourceViewer, new ICodeLensProvider[] { new ClassReferencesCodeLensProvider()
				,				new ClassImplementationsCodeLensProvider() });
		manager.refresh();

		sourceViewer.getTextWidget().addModifyListener(e -> {
			manager.refresh();
		});

		//shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
