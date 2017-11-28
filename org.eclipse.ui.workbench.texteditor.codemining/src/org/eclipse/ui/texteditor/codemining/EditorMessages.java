/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. */
package org.eclipse.ui.texteditor.codemining;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


/**
 * Helper class to get NLSed messages.
 */
final class EditorMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.texteditor.ConstructedEditorMessages";//$NON-NLS-1$
	private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
	public static String Editor_error_CodeMiningProviderTarget_invalidExtension_message;
	public static String Editor_error_CodeMiningProviderTarget_invalidElementName_message;

	/**
	 * Returns the message bundle which contains constructed keys.
	 *
	 * @since 3.1
	 * @return the message bundle
	 */
	public static ResourceBundle getBundleForConstructedKeys() {
		return fgBundleForConstructedKeys;
	}

	private static final String BUNDLE_NAME= EditorMessages.class.getName();

	private EditorMessages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}
}