package org.eclipse.ui.internal.editors.text.codemining;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.codemining.CodeMiningProviderRegistry;

public class EditorsPlugin2 extends AbstractUIPlugin {

	private static EditorsPlugin2 fgInstance;

	public static EditorsPlugin2 getDefault() {
		return fgInstance;
	}

	private CodeMiningProviderRegistry fCodeMiningProviderRegistry;

	/**
	 * Returns the registry that contains the hyperlink detectors contributed by the
	 * <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code> extension
	 * point.
	 *
	 * @return the hyperlink detector registry
	 * @since 3.3
	 */
	public synchronized CodeMiningProviderRegistry getCodeMiningProviderRegistry() {
		if (fCodeMiningProviderRegistry == null)
			fCodeMiningProviderRegistry = new CodeMiningProviderRegistry(getPreferenceStore());
		return fCodeMiningProviderRegistry;
	}

	public EditorsPlugin2() {
		Assert.isTrue(fgInstance == null);
		fgInstance = this;

	}
}
