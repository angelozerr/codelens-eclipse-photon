package org.eclipse.jface.text.codelens;

import org.eclipse.core.runtime.NullProgressMonitor;

class CodeLensMonitor extends NullProgressMonitor {

	@Override
	public boolean isCanceled() {
		if (super.isCanceled()) {
			System.err.println("cancel!!");
		}
		return super.isCanceled();
	}
}
