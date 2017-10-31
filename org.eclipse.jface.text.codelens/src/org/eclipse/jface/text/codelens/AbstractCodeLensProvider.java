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
	private IAdaptable fContext;

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
		if (fContext != null)
			throw new IllegalStateException();
		fContext = context;
	}

	@Override
	public void dispose() {
		fContext = null;
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
		if (fContext != null)
			return fContext.getAdapter(adapterClass);
		return null;
	}

}
