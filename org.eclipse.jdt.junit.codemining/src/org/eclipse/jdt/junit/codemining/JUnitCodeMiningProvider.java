package org.eclipse.jdt.junit.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.ui.texteditor.ITextEditor;

public class JUnitCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			monitor.isCanceled();
			ITextEditor textEditor = super.getAdapter(ITextEditor.class);
			ITypeRoot unit = EditorUtility.getEditorInputJavaElement(textEditor, true);
			if (unit == null) {
				return null;
			}
			try {
				IJavaElement[] elements = unit.getChildren();
				List<ICodeMining> minings = new ArrayList<>(elements.length);
				collectCodeMinings(unit, elements, minings, viewer, monitor);
				monitor.isCanceled();
				return minings;
			} catch (JavaModelException e) {
				// TODO: what should we done when there are some errors?
			}
			return null;
		});
	}

	private void collectCodeMinings(ITypeRoot unit, IJavaElement[] elements, List<ICodeMining> minings,
			ITextViewer viewer, IProgressMonitor monitor) {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			try {
				if (element.getElementType() == IJavaElement.TYPE) {
					collectCodeMinings(unit, ((IType) element).getChildren(), minings, viewer, monitor);
				} else if (element.getElementType() == IJavaElement.METHOD) {
					IMethod method = (IMethod) element;
					if (isTestMethod(method, "org.junit.Test")) {
						minings.add(new JUnitCodeMining(element, viewer.getDocument(), this));
					}
						//&& CoreTestSearchEngine.isTestOrTestSuite((((IMethod) element).getDeclaringType()))) {
					//IMethod type = ((IMethod) element;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static boolean isTestMethod(IMethod method, String annotation) {
		int flags;
		try {
			flags = method.getFlags();
			// 'V' is void signature
			return !(method.isConstructor() || !Flags.isPublic(flags) || Flags.isAbstract(flags) || Flags.isStatic(flags) || !"V".equals(method.getReturnType())) && method.getAnnotation(annotation).exists();
		} catch (JavaModelException e) {
			// ignore
			return false;
		}
	}

}
