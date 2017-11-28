package org.eclipse.ui.texteditor.codemining;

import org.eclipse.core.runtime.IConfigurationElement;

public class CodeMiningDescriptor {

	private final IConfigurationElement element;

	public CodeMiningDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	public String getId() {
		return element.getAttribute("id");
	}

	public String getName() {
		return element.getAttribute("name");
	}

	public String getDescription() {
		return element.getAttribute("description");
	}
}
