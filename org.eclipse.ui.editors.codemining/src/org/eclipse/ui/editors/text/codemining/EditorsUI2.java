package org.eclipse.ui.editors.text.codemining;

import org.eclipse.ui.internal.editors.text.codemining.EditorsPlugin2;
import org.eclipse.ui.texteditor.codemining.CodeMiningProviderRegistry;

public class EditorsUI2 {

	/**
	 * Returns the registry that contains the hyperlink detectors contributed by the
	 * <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code> extension
	 * point.
	 *
	 * @return the hyperlink detector registry
	 * @since 3.3
	 */
	public static CodeMiningProviderRegistry getCodeMiningProviderRegistry() {
		return EditorsPlugin2.getDefault().getCodeMiningProviderRegistry();
	}
}
