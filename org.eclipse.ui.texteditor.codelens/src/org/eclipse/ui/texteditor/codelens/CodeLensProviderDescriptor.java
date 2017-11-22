package org.eclipse.ui.texteditor.codelens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.codelens.ICodeLensProvider;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.codelens.internal.EditorMessages;
import org.osgi.framework.Bundle;

/**
 * Describes a contribution to the 'org.eclipse.ui.workbench.texteditor.codeLensProviders'
 * extension point.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class CodeLensProviderDescriptor {

	private static final String CODELENS_PROVIDERS_EXTENSION_POINT= "org.eclipse.ui.workbench.texteditor.codeLensProviders"; //$NON-NLS-1$
	private static final String CODELENS_PROVIDERS_ELEMENT= "codeLensProviders"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	
	private IConfigurationElement fElement;
	//private CodeLensProviderTargetDescriptor fTarget;


	/**
	 * Returns descriptors for all hyperlink detector extensions.
	 *
	 * @return an array with the contributed hyperlink detectors
	 */
	public static CodeLensProviderDescriptor[] getContributedCodeLensProviders() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(CODELENS_PROVIDERS_EXTENSION_POINT);
		CodeLensProviderDescriptor[] codeLensProviderDescs= createDescriptors(elements);
		return codeLensProviderDescs;
	}

	/**
	 * Creates a new descriptor from the given configuration element.
	 *
	 * @param element the configuration element
	 */
	private CodeLensProviderDescriptor(IConfigurationElement element) {
		Assert.isNotNull(element);
		fElement= element;
	}

	/**
	 * Creates a new {@link ICodeLensProvider}.
	 *
	 * @return the hyperlink detector or <code>null</code> if the plug-in isn't loaded yet
	 * @throws CoreException if a failure occurred during creation
	 * @since 3.9
	 */
	public ICodeLensProvider createCodeLensProviderImplementation() throws CoreException {
		final Throwable[] exception= new Throwable[1];
		final ICodeLensProvider[] result= new ICodeLensProvider[1];
		String message= NLSUtility.format(EditorMessages.Editor_error_CodeLensProvider_couldNotCreate_message, new String[] { getId(), fElement.getContributor().getName() });
		ISafeRunnable code= new SafeRunnable(message) {
			@Override
			public void run() throws Exception {
		 		String pluginId = fElement.getContributor().getName();
				boolean isPlugInActivated= Platform.getBundle(pluginId).getState() == Bundle.ACTIVE;
				if (isPlugInActivated /*|| canActivatePlugIn()*/)
					result[0]= (ICodeLensProvider)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			@Override
			public void handleException(Throwable ex) {
				super.handleException(ex);
				exception[0]= ex;
			}

		};

		SafeRunner.run(code);

		if (exception[0] == null)
			return result[0];
		throw new CoreException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, exception[0]));

	}

	public Object getTargetId() {
		// TODO Auto-generated method stub
		return null;
	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the hyperlink detector's id.
	 *
	 * @return the hyperlink detector's id
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector's name.
	 *
	 * @return the hyperlink detector's name
	 */
	public String getName() {
		return fElement.getAttribute(NAME_ATTRIBUTE);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()) || getId() == null)
			return false;
		return getId().equals(((CodeLensProviderDescriptor)obj).getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	private static CodeLensProviderDescriptor[] createDescriptors(IConfigurationElement[] elements) {
		//CodeLensProviderTargetDescriptor[] targets= CodeLensProviderTargetDescriptor.getContributedCodeLensProviderTargets();
		List<CodeLensProviderDescriptor> result= new ArrayList<>(elements.length);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (CODELENS_PROVIDERS_ELEMENT.equals(element.getName())) {
				CodeLensProviderDescriptor desc= new CodeLensProviderDescriptor(element);
				//if (desc.isValid(targets))
					result.add(desc);
				/*else {
					String message= NLSUtility.format(EditorMessages.Editor_error_CodeLensProvider_invalidExtension_message, new String[] {desc.getId(), element.getContributor().getName()});
					TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
				}*/
			} else {
				String message= NLSUtility.format(EditorMessages.Editor_error_CodeLensProvider_invalidElementName_message, new String[] { element.getContributor().getName(), element.getName() });
				TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
			}
		}
		return result.toArray(new CodeLensProviderDescriptor[result.size()]);
	}
}
