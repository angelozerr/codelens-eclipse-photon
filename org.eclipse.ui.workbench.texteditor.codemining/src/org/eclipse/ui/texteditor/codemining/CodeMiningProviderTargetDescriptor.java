package org.eclipse.ui.texteditor.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Describes a contribution to the 'org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets'
 * extension point.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class CodeMiningProviderTargetDescriptor {

	private static final String HYPERLINK_DETECTOR_TARGETS_EXTENSION_POINT= "org.eclipse.ui.workbench.texteditor.codemining.codeMiningProviderTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT= "target"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String CONTEXT_ELEMENT= "context"; //$NON-NLS-1$
	private static final String TYPE_ATTRIBUTE= "type"; //$NON-NLS-1$

	private IConfigurationElement fElement;


	/**
	 * Returns descriptors for all hyperlink detector extensions.
	 *
	 * @return an array with the contributed hyperlink detectors
	 */
	public static CodeMiningProviderTargetDescriptor[] getContributedCodeMiningProviderTargets() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(HYPERLINK_DETECTOR_TARGETS_EXTENSION_POINT);
		CodeMiningProviderTargetDescriptor[] hyperlinkDetectorDescs= createDescriptors(elements);
		return hyperlinkDetectorDescs;
	}

	/**
	 * Creates a new descriptor from the given configuration element.
	 *
	 * @param element the configuration element
	 */
	private CodeMiningProviderTargetDescriptor(IConfigurationElement element) {
		Assert.isNotNull(element);
		fElement= element;
	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the hyperlink detector target's id.
	 *
	 * @return the hyperlink detector target's id
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector target's name.
	 *
	 * @return the hyperlink detector target's name
	 */
	public String getName() {
		return fElement.getAttribute(NAME_ATTRIBUTE);
	}

	/**
	 * Returns the types that the context of this
	 * hyperlink detector target supports.
	 *
	 * @return an array with type names that this target's context supports
	 */
	public String[] getTypes() {
		IConfigurationElement[] contexts= fElement.getChildren(CONTEXT_ELEMENT);
		String[] types= new String[contexts.length];
		for (int i= 0; i < contexts.length; i++)
			types[i]= contexts[i].getAttribute(TYPE_ATTRIBUTE);
		return types;
	}

	/**
	 * Returns the hyperlink detector target's description.
	 *
	 * @return the hyperlink detector target's description or <code>null</code> if not provided
	 */
	public String getDescription() {
		return fElement.getAttribute(DESCRIPTION_ATTRIBUTE);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()) || getId() == null)
			return false;
		return getId().equals(((CodeMiningProviderTargetDescriptor)obj).getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	private static CodeMiningProviderTargetDescriptor[] createDescriptors(IConfigurationElement[] elements) {
		List<CodeMiningProviderTargetDescriptor> result= new ArrayList<>(elements.length);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (TARGET_ELEMENT.equals(element.getName())) {
				CodeMiningProviderTargetDescriptor desc= new CodeMiningProviderTargetDescriptor(element);
				if (desc.isValid())
					result.add(desc);
				else {
					String message= NLSUtility.format(EditorMessages.Editor_error_CodeMiningProviderTarget_invalidExtension_message, new String[] {desc.getId(), element.getContributor().getName()});
					TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
				}
			} else {
				String message= NLSUtility.format(EditorMessages.Editor_error_CodeMiningProviderTarget_invalidElementName_message, new String[] { element.getContributor().getName(), element.getName() });
				TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
			}

		}
		return result.toArray(new CodeMiningProviderTargetDescriptor[result.size()]);
	}

	private boolean isValid() {
		return getId() != null && getName() != null;
	}

}
