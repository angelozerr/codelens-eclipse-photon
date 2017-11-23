/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.examples.sources.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.BlockAnnotation;
import org.eclipse.swt.custom.StyledText;

public class ColorStatusAnnotation extends BlockAnnotation {

	public ColorStatusAnnotation(Position position, StyledText styledText) {
		super(position, styledText);
	}

	public void setStatus(String status) {
		super.setText(status);
	}

}
