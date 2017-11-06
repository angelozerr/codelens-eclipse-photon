package org.eclipse.ui.texteditor.codelens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codelens.AbstractSyncCodeLensProvider;
import org.eclipse.jface.text.codelens.Command;
import org.eclipse.jface.text.codelens.ICodeLens;

public class ClassReferencesCodeLensProvider extends AbstractSyncCodeLensProvider {

	@Override
	protected List<? extends ICodeLens> provideSyncCodeLenses(ITextViewer viewer, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		List<ICodeLens> lenses = new ArrayList<>();
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false);
			int index = line.indexOf("class ");
			if (index != -1) {
				String className = line.substring(index + "class ".length(), line.length());
				index = className.indexOf(" ");
				if (index != -1) {
					className = className.substring(0, index);
				}
				if (className.length() > 0) {
					lenses.add(new ClassCodeLens(className, i + 1, this));
				}
			}
		}
		return lenses;
	}

	@Override
	protected ICodeLens resolveSyncCodeLens(ITextViewer viewer, ICodeLens codeLens, IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		String className = ((ClassCodeLens) codeLens).getClassName();
		int refCount = 0;
		int lineCount = document.getNumberOfLines();
		for (int i = 0; i < lineCount; i++) {
			String line = getLineText(document, i, false);
			refCount += line.contains("new " + className) ? 1 : 0;
		}
		((ClassCodeLens) codeLens).setCommand(new Command(refCount + " references", ""));
		return codeLens;
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
