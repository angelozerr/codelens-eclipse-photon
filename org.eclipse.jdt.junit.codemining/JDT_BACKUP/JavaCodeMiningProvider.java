/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide Java References/Implementation CodeMinings - Bug 529127
 */
package org.eclipse.jdt.internal.ui.javaeditor.codemining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * Java code mining provider to show Java references and implementations code minings.
 * 
 * @since 3.14
 *
 */
public class JavaCodeMiningProvider extends AbstractCodeMiningProvider {

	private static Set<String> SILENCED_CODEGENS= Collections.singleton("lombok"); //$NON-NLS-1$

	private final class CreateMarkerForWrongScopeReferenceRequestor extends SearchRequestor {

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			System.err.println(match);
		}
		
	}
	private boolean isReferencesCodeMiningsEnabled() {
		// TODO: search that from preferences
		return true;
	}

	private boolean isImplementationsCodeMiningsEnabled() {
		// TODO: search that from preferences
		return true;
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			monitor.isCanceled();
			ITextEditor textEditor= super.getAdapter(ITextEditor.class);
			ITypeRoot unit= EditorUtility.getEditorInputJavaElement(textEditor, true);
			if (unit == null) {
				return null;
			}
			try {
				IJavaElement[] elements= unit.getChildren();
				List<ICodeMining> minings= new ArrayList<>(elements.length);				
				collectLineHeaderCodeMinings(unit, textEditor, unit.getChildren(), minings, viewer, monitor);
				monitor.isCanceled();
				collectLineContentCodeMinings(unit, textEditor, minings, viewer, monitor);
				monitor.isCanceled();
				return minings;
			} catch (JavaModelException e) {
				// TODO: what should we done when there are some errors?
			}
			return null;
		});
	}

	private void collectLineContentCodeMinings(ITypeRoot unit, ITextEditor textEditor, List<ICodeMining> minings, ITextViewer viewer, IProgressMonitor monitor) {
		
		CompilationUnit cu = getCompilationUnitNode(unit, true);
		
		CalleeAnalyzerVisitor visitor = new CalleeAnalyzerVisitor(cu, minings, this);
		cu.accept(visitor);
		
		
//		final SearchEngine searchEngine= new SearchEngine(new ICompilationUnit[] { (ICompilationUnit) unit });
//		SearchPattern pattern= new TypeReferencePattern(null, null, SearchPattern.R_PATTERN_MATCH);
//		SearchRequestor requestor = new CreateMarkerForWrongScopeReferenceRequestor();
//		try {
//			searchEngine.search(pattern,
//					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
//					SearchEngine.createJavaSearchScope(new IJavaElement[] { unit }),
//					requestor, monitor);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	 static CompilationUnit getCompilationUnitNode(ITypeRoot typeRoot, boolean resolveBindings) {
	        try {
		    	if (typeRoot.exists() && typeRoot.getBuffer() != null) {
					ASTParser parser= ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
					parser.setSource(typeRoot);
					parser.setResolveBindings(resolveBindings);
					return (CompilationUnit) parser.createAST(null);
		    	}
	        } catch (JavaModelException e) {
	            JavaPlugin.log(e);
	        }
	        return null;
	    }

	/**
	 * Collect java code minings
	 * 
	 * @param unit the compilation unit
	 * @param textEditor the Java editor
	 * @param elements the java elements to track
	 * @param minings the current list of minings to update
	 * @param viewer the viewer
	 * @param monitor the monitor
	 * @throws JavaModelException
	 */
	private void collectLineHeaderCodeMinings(ITypeRoot unit, ITextEditor textEditor, IJavaElement[] elements, List<ICodeMining> minings,
			ITextViewer viewer, IProgressMonitor monitor) throws JavaModelException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				collectLineHeaderCodeMinings(unit, textEditor, ((IType) element).getChildren(), minings, viewer, monitor);
			} else if (element.getElementType() != IJavaElement.METHOD || isHiddenGeneratedElement(element)) {
				continue;
			}

			if (isReferencesCodeMiningsEnabled()) {
				try {
					minings.add(new JavaReferenceCodeMining(element, (JavaEditor) textEditor, viewer.getDocument(), this));
				} catch (BadLocationException e) {
					// TODO: what should we done when there are some errors?
				}
			}
			if (element.getElementType() == IJavaElement.METHOD) {
				IMethod method= (IMethod) element;
				method.getParameterNames();
			}

			if (isImplementationsCodeMiningsEnabled()) {
				if (element instanceof IType) {
					IType type= (IType) element;
					if (type.isInterface() || Flags.isAbstract(type.getFlags())) {
						try {
							minings.add(new JavaImplementationCodeMining(type, viewer.getDocument(), this));
						} catch (BadLocationException e) {
							// TODO: what should we done when there are some errors?
						}
					}
				}
			}
		}
	}

	/**
	 * This code is a copy/paste of
	 * https://github.com/eclipse/eclipse.jdt.ls/blob/master/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/JDTUtils.java#L669
	 * 
	 * @param element
	 * @return
	 */
	private static boolean isHiddenGeneratedElement(IJavaElement element) {
		// generated elements are tagged with javax.annotation.Generated and
		// they need to be filtered out
		if (element instanceof IAnnotatable) {
			try {
				IAnnotation[] annotations= ((IAnnotatable) element).getAnnotations();
				if (annotations.length != 0) {
					for (IAnnotation annotation : annotations) {
						if (isSilencedGeneratedAnnotation(annotation)) {
							return true;
						}
					}
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return false;
	}

	/**
	 * This code is a copy/paste of
	 * https://github.com/eclipse/eclipse.jdt.ls/blob/master/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/JDTUtils.java#L689
	 * 
	 * @param annotation
	 * @return
	 * @throws JavaModelException
	 */
	private static boolean isSilencedGeneratedAnnotation(IAnnotation annotation) throws JavaModelException {
		if ("javax.annotation.Generated".equals(annotation.getElementName())) { //$NON-NLS-1$
			IMemberValuePair[] memberValuePairs= annotation.getMemberValuePairs();
			for (IMemberValuePair m : memberValuePairs) {
				if ("value".equals(m.getMemberName()) && IMemberValuePair.K_STRING == m.getValueKind()) { //$NON-NLS-1$
					if (m.getValue() instanceof String) {
						return SILENCED_CODEGENS.contains(m.getValue());
					} else if (m.getValue() instanceof Object[]) {
						for (Object val : (Object[]) m.getValue()) {
							if (SILENCED_CODEGENS.contains(val)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
