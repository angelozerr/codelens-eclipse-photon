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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A codelens provider that can provide adapters through a context that can be
 * set by the creator of this codelens provider.
 * <p>
 * Clients may subclass.
 * </p>
 *
 */
public abstract class AbstractCodeLensProvider implements ICodeLensProvider {

	/**
	 * The context of this codelens provider.
	 */
	private IAdaptable context;

	/**
	 * Sets this codelens provider's context which is responsible to provide the
	 * adapters.
	 *
	 * @param context
	 *            the context for this codelens provider
	 * @throws IllegalArgumentException
	 *             if the context is <code>null</code>
	 * @throws IllegalStateException
	 *             if this method is called more than once
	 */
	public final void setContext(IAdaptable context) throws IllegalStateException, IllegalArgumentException {
		Assert.isLegal(context != null);
		if (this.context != null)
			throw new IllegalStateException();
		this.context = context;
	}

	@Override
	public void dispose() {
		context = null;
	}

	/**
	 * Returns an object which is an instance of the given class and provides
	 * additional context for this codelens provider.
	 *
	 * @param adapterClass
	 *            the adapter class to look up
	 * @return an instance that can be cast to the given class, or <code>null</code>
	 *         if this object does not have an adapter for the given class
	 */
	protected final <T> T getAdapter(Class<T> adapterClass) {
		Assert.isLegal(adapterClass != null);
		if (context != null)
			return context.getAdapter(adapterClass);
		return null;
	}

}
