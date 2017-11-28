package org.eclipse.ui.internal.editors.text.codemining;
import org.eclipse.osgi.util.NLS;


/**
 * Helper class to get NLSed messages.
 *
 * @since 2.1
 */
final class TextEditorMessages2 extends NLS {

	private static final String BUNDLE_NAME= TextEditorMessages2.class.getName();

	private TextEditorMessages2() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, TextEditorMessages2.class);
	}

	public static String CodeMiningsConfigurationBlock_description;
	public static String CodeMiningProviderTree_nameColumn;
	public static String CodeMiningProviderTree_targetNameColumn;
}
