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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.codemining.CodeMiningManager;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
//import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Code Mining demo with class references, implementations.
 *
 */
public class CodeMiningWithPreferencesDemo {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		shell.setText("Code Mining demo");

		ISourceViewer sourceViewer = new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.getTextWidget().setLayoutData(new GridData(GridData.FILL_BOTH));
		sourceViewer.setDocument(
				new Document("// Type class & new keyword and see references CodeMining\n"
						+ "// Name class with a number N to emulate Nms before resolving the references CodeMining \n\n"
						+ "class A\n" + "new A\n" + "new A\n\n" + "class 5\n" + "new 5\n" + "new 5\n" + "new 5"),
				new AnnotationModel());
		// Add AnnotationPainter (required by CodeMining)
		AnnotationPainter painter = createAnnotationPainter(sourceViewer);

		IPreferenceStore store = new PreferenceStore();
		store.setValue("codemining.references.enabled", true);
		store.setValue("codemining.implementation.enabled", true);
		
		CodeMiningManager manager = new CodeMiningManager();
		manager.install(sourceViewer, painter, new ICodeMiningProvider[] { new ClassReferencesCodeMiningProvider(),
				new ClassImplementationsCodeMiningProvider() });
		manager.configure(store);
		manager.run();
		
		sourceViewer.getTextWidget().addModifyListener(e -> {
			manager.run();
		});
		
		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(new GridLayout());
		
		Button showReferences = new Button(buttons, SWT.TOGGLE);
		showReferences.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showReferences.setSelection(store.getBoolean("codemining.references.enabled"));
		showReferences.setText("Show References");
		showReferences.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue("codemining.references.enabled", showReferences.getSelection());
			}
		});

		Button showImplementations = new Button(buttons, SWT.TOGGLE);
		showImplementations.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showImplementations.setSelection(store.getBoolean("codemining.implementations.enabled"));
		showImplementations.setText("Show Implementations");
		showImplementations.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue("codemining.implementations.enabled", showImplementations.getSelection());
			}
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

}
