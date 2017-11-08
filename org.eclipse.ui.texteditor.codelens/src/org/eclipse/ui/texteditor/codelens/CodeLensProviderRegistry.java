/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeLens support - Bug XXXXXX
 */
package org.eclipse.ui.texteditor.codelens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codelens.AbstractCodeLensProvider;
import org.eclipse.jface.text.codelens.ICodeLens;
import org.eclipse.jface.text.codelens.ICodeLensProvider;
import org.eclipse.jface.text.codelens.ICodeLensResolver;

public class CodeLensProviderRegistry {

	/**
	 * Delegate for contributed codelens provider.
	 */
	private class CodeLensProviderDelegate implements ICodeLensProvider, ICodeLensResolver {

		private CodeLensProviderDescriptor codeLensProviderDescriptor;
		private ICodeLensProvider fCodeLensProvider;
		private boolean fFailedDuringCreation = false;
		private IAdaptable fContext;
		private int fStateMask;
		private boolean fIsEnabled;

		private CodeLensProviderDelegate(CodeLensProviderDescriptor descriptor) {
			codeLensProviderDescriptor = descriptor;
			if (fPreferenceStore != null) {
				fStateMask = fPreferenceStore
						.getInt(codeLensProviderDescriptor.getId() /* + CodeLensProviderDescriptor.STATE_MASK_POSTFIX */);
				fIsEnabled = !fPreferenceStore.getBoolean(codeLensProviderDescriptor.getId());
			}
		}

		@Override
		public CompletableFuture<List<? extends ICodeLens>> provideCodeLenses(ITextViewer viewer,
				IProgressMonitor monitor) {
			if (!isEnabled())
				return null;

			if (!fFailedDuringCreation && fCodeLensProvider == null) {
				try {
					fCodeLensProvider = codeLensProviderDescriptor.createCodeLensProviderImplementation();
				} catch (CoreException ex) {
					fFailedDuringCreation = true;
				}
				if (fContext != null && fCodeLensProvider instanceof AbstractCodeLensProvider)
					((AbstractCodeLensProvider) fCodeLensProvider).setContext(fContext);
			}
			if (fCodeLensProvider != null)
				return fCodeLensProvider.provideCodeLenses(viewer, monitor);

			return null;

		}

		@Override
		public CompletableFuture<ICodeLens> resolveCodeLens(ITextViewer viewer, ICodeLens codeLens,
				IProgressMonitor monitor) {
			if (!isEnabled())
				return null;

			if (!fFailedDuringCreation && fCodeLensProvider == null) {
				try {
					fCodeLensProvider = codeLensProviderDescriptor.createCodeLensProviderImplementation();
				} catch (CoreException ex) {
					fFailedDuringCreation = true;
				}
				if (fContext != null && fCodeLensProvider instanceof AbstractCodeLensProvider)
					((AbstractCodeLensProvider) fCodeLensProvider).setContext(fContext);
			}
			if (fCodeLensProvider != null && fCodeLensProvider instanceof ICodeLensResolver)
				return ((ICodeLensResolver) fCodeLensProvider).resolveCodeLens(viewer, codeLens, monitor);

			return null;

		}

		private boolean isEnabled() {
			return fIsEnabled;
		}

		private void setContext(IAdaptable context) {
			fContext = context;
		}

		@Override
		public void dispose() {
			fCodeLensProvider.dispose();
			fCodeLensProvider = null;
			codeLensProviderDescriptor = null;
			fContext = null;
		}

		// @Override
		// public int getStateMask() {
		// return fStateMask;
		// }

	}

	private CodeLensProviderDescriptor[] fCodeLensProviderDescriptors;
	private IPreferenceStore fPreferenceStore;

	/**
	 * Creates a new codelens provider registry.
	 */
	public CodeLensProviderRegistry() {
	}

	/**
	 * Creates a new codelens provider registry that controls codelens enablement
	 * via the given preference store.
	 * <p>
	 * The codelens provider id is used as preference key. The value is of type
	 * <code>Boolean</code> where <code>false</code> means that the codelens
	 * provider is active.
	 * </p>
	 *
	 * @param preferenceStore
	 *            the preference store to be used
	 */
	public CodeLensProviderRegistry(IPreferenceStore preferenceStore) {
		fPreferenceStore = preferenceStore;
	}

	/**
	 * Returns all codelens providers contributed to the workbench.
	 *
	 * @return an array of codelens provider descriptors
	 */
	public synchronized CodeLensProviderDescriptor[] getCodeLensProviderDescriptors() {
		initCodeLensProviderDescriptors();
		CodeLensProviderDescriptor[] result = new CodeLensProviderDescriptor[fCodeLensProviderDescriptors.length];
		System.arraycopy(fCodeLensProviderDescriptors, 0, result, 0, fCodeLensProviderDescriptors.length);
		return result;
	}

	/**
	 * Initializes the codelens provider descriptors.
	 */
	private synchronized void initCodeLensProviderDescriptors() {
		if (fCodeLensProviderDescriptors == null)
			fCodeLensProviderDescriptors = CodeLensProviderDescriptor.getContributedCodeLensProviders();
	}

	public ICodeLensProvider[] createCodeLensProviders(String targetId, IAdaptable context) {
		Assert.isLegal(targetId != null);
		initCodeLensProviderDescriptors();

		List<CodeLensProviderDelegate> result = new ArrayList<>();
		for (int i = 0; i < fCodeLensProviderDescriptors.length; i++) {
			if (targetId.equals(fCodeLensProviderDescriptors[i].getTargetId())) {
				CodeLensProviderDelegate detector = new CodeLensProviderDelegate(fCodeLensProviderDescriptors[i]);
				result.add(detector);
				detector.setContext(context);
			}
		}
		return result.toArray(new ICodeLensProvider[result.size()]);
	}
}
