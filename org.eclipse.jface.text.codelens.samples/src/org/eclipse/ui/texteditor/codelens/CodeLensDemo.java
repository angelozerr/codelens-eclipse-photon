package org.eclipse.ui.texteditor.codelens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CodeLensDemo {

	public static void main(String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Line spacing provider in action");

		StyledText text = new StyledText(shell, SWT.BORDER | SWT.V_SCROLL);
		text.setText("// Type your custom line spacing \n10\n5\nabcd\n20\nefgh");

		text.setLineSpacingProvider(lineIndex -> {
			String line = text.getLine(lineIndex).trim();
			try {
				return Integer.parseInt(line);
			} catch(NumberFormatException e) {
				return null;
			}
		});

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
