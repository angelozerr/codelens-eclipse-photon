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

import org.eclipse.jface.text.Position;

/**
 * CodeLens API
 *
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

	Command getCommand();

	ICodeLensResolver getResolver();

	boolean isResolved();
}
