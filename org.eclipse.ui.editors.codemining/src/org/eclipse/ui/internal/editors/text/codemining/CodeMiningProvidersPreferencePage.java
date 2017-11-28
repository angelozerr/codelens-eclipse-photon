/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. */
package org.eclipse.ui.internal.editors.text.codemining;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock;
import org.eclipse.ui.internal.editors.text.OverlayPreferenceStore;

/**
 * CodeMining providers preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 *
 * @since 3.3
 */
public class CodeMiningProvidersPreferencePage extends AbstractConfigurationBlockPreferencePage {

	@Override
	protected String getHelpId() {
		return ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE;
	}

	@Override
	protected void setDescription() {
		String description= TextEditorMessages2.CodeMiningsConfigurationBlock_description;
		setDescription(description);
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return super.createDescriptionLabel(parent);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new CodeMiningProvidersConfigurationBlock(this, overlayPreferenceStore);
	}
}
