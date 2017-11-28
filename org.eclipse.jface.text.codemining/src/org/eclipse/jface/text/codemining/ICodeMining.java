/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.codemining;

import org.eclipse.jface.text.Position;

/**
 * Code Mining API
 *
 * @since 3.13.0
 */
public interface ICodeMining {

	/**
	 * Returns the line position where content mining must be displayed in the line
	 * spacing area.
	 * 
	 * @return the line position where content mining must be displayed in the line
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
	 * Returns the content mining resolver and null otherwise.
	 * 
	 * @return the content mining resolver and null otherwise.
	 */
	ICodeMiningResolver getResolver();

	/**
	 * Returns true if the content mining is resolved and false otherwise.
	 * 
	 * @return true if the content mining is resolved and false otherwise.
	 */
	boolean isResolved();
}
