/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeMining support - Bug XXXXXX
 */
package org.eclipse.ui.texteditor.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;

public class CodeMiningProviderRegistry {

	/**
	 * Delegate for contributed codelens provider.
	 */
	private class CodeMiningProviderDelegate implements ICodeMiningProvider {

		private CodeMiningProviderDescriptor codeLensProviderDescriptor;
		private ICodeMiningProvider fCodeMiningProvider;
		private boolean fFailedDuringCreation = false;
		private IAdaptable fContext;
		private int fStateMask;
		private boolean fIsEnabled;

		private CodeMiningProviderDelegate(CodeMiningProviderDescriptor descriptor) {
			codeLensProviderDescriptor = descriptor;
			if (fPreferenceStore != null) {
				fStateMask = fPreferenceStore.getInt(
						codeLensProviderDescriptor.getId() /* + CodeMiningProviderDescriptor.STATE_MASK_POSTFIX */);
				fIsEnabled = !fPreferenceStore.getBoolean(codeLensProviderDescriptor.getId());
			}
		}

		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
				IProgressMonitor monitor) {
			if (!isEnabled())
				return null;

			if (!fFailedDuringCreation && fCodeMiningProvider == null) {
				try {
					fCodeMiningProvider = codeLensProviderDescriptor.createCodeMiningProviderImplementation();
				} catch (CoreException ex) {
					fFailedDuringCreation = true;
				}
				if (fContext != null && fCodeMiningProvider instanceof AbstractCodeMiningProvider)
					((AbstractCodeMiningProvider) fCodeMiningProvider).setContext(fContext);
			}
			if (fCodeMiningProvider != null)
				return fCodeMiningProvider.provideCodeMinings(viewer, monitor);

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
			fCodeMiningProvider.dispose();
			fCodeMiningProvider = null;
			codeLensProviderDescriptor = null;
			fContext = null;
		}

//		@Override
//		public void configure(IPreferenceStore store) {
//			if (fCodeMiningProvider != null) {
//				fCodeMiningProvider.configure(store);
//			}
//
//		}

		// @Override
		// public int getStateMask() {
		// return fStateMask;
		// }

	}

	private CodeMiningProviderDescriptor[] fCodeMiningProviderDescriptors;
	private IPreferenceStore fPreferenceStore;

	/**
	 * Creates a new codelens provider registry.
	 */
	public CodeMiningProviderRegistry() {
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
	public CodeMiningProviderRegistry(IPreferenceStore preferenceStore) {
		fPreferenceStore = preferenceStore;
	}

	/**
	 * Returns all codelens providers contributed to the workbench.
	 *
	 * @return an array of codelens provider descriptors
	 */
	public synchronized CodeMiningProviderDescriptor[] getCodeMiningProviderDescriptors() {
		initCodeMiningProviderDescriptors();
		CodeMiningProviderDescriptor[] result = new CodeMiningProviderDescriptor[fCodeMiningProviderDescriptors.length];
		System.arraycopy(fCodeMiningProviderDescriptors, 0, result, 0, fCodeMiningProviderDescriptors.length);
		return result;
	}

	/**
	 * Initializes the codelens provider descriptors.
	 */
	private synchronized void initCodeMiningProviderDescriptors() {
		if (fCodeMiningProviderDescriptors == null)
			fCodeMiningProviderDescriptors = CodeMiningProviderDescriptor.getContributedCodeMiningProviders();
	}

	public ICodeMiningProvider[] createCodeMiningProviders(String targetId, IAdaptable context) {
		Assert.isLegal(targetId != null);
		initCodeMiningProviderDescriptors();

		List<CodeMiningProviderDelegate> result = new ArrayList<>();
		for (int i = 0; i < fCodeMiningProviderDescriptors.length; i++) {
			if (targetId.equals(fCodeMiningProviderDescriptors[i].getTargetId())) {
				CodeMiningProviderDelegate detector = new CodeMiningProviderDelegate(fCodeMiningProviderDescriptors[i]);
				result.add(detector);
				detector.setContext(context);
			}
		}
		return result.toArray(new ICodeMiningProvider[result.size()]);
	}
}
