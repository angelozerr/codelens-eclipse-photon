/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - CodeLens support - Bug 526969
 */
package org.eclipse.jface.text.codelens;

import org.eclipse.jface.text.Position;

/**
 * CodeLens API
 *
 * @since 3.107
 */
public interface ICodeLens {

	/**
	 * Returns the line position where code lens must be displayed in the line
	 * spacing area.
	 * 
	 * @return the line position where code lens must be displayed in the line
	 *         spacing area.
	 */
	Position getPosition();

	/**
	 * Returns the resolved command and null otherwise.
	 * 
	 * @return the resolved command and null otherwise.
	 */
	Command getCommand();

	/**
	 * Returns the codelens resolver and null otherwise.
	 * 
	 * @return the codelens resolver and null otherwise.
	 */
	ICodeLensResolver getResolver();

	/**
	 * Returns true if the codelens is resolved and false otherwise.
	 * 
	 * @return true if the codelens is resolved and false otherwise.
	 */
	boolean isResolved();
}
