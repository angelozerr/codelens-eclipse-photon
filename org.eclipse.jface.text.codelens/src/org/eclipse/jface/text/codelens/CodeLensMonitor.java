/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Provide CodeLens support - Bug XXXXXX
 */
package org.eclipse.jface.text.codelens;

import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Monitor which throws a {@link CancellationException} when
 * {@link IProgressMonitor#isCanceled()} is called.
 *
 */
class CodeLensMonitor extends NullProgressMonitor {

	@Override
	public boolean isCanceled() {
		boolean canceled = super.isCanceled();
		if (canceled) {
			throw new CancellationException();
		}
		return canceled;
	}
}
