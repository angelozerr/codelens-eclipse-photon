package org.eclipse.jdt.junit.codemining;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.inlined.IInlinedAnnotationAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;

public class JUnitLaunchCodeMining extends AbstractJavaCodeMining {

	private final StyledText styledText;
	private final String mode;

	public JUnitLaunchCodeMining(IJavaElement element, StyledText styledText, String label, String mode,
			IDocument document, ICodeMiningProvider provider) throws JavaModelException, BadLocationException {
		super(element, document, provider);
		this.styledText = styledText;
		this.mode = mode;
		super.setLabel(label);
	}

	private StyledText getTextWidget() {
		return styledText;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	// @Override
	// public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
	// Image image = getImage();
	// gc.drawImage(image, x, y + gc.getFontMetrics().getDescent());
	// Rectangle bounds = image.getBounds();
	// return new Point(bounds.width, bounds.height);
	// }
	//
	// private Image getImage() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public IInlinedAnnotationAction getAction() {
		return new IInlinedAnnotationAction() {

			@Override
			public void activate() {
				StyledText styledText = getTextWidget();
				styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			}

			@Override
			public void unactivate() {
				StyledText styledText = getTextWidget();
				styledText.setCursor(null);
			}

			@Override
			public void click(MouseEvent e) {
				JUnitLaunchShortcut shortcut = new JUnitLaunchShortcut();
				shortcut.launch(new StructuredSelection(getElement()), mode);
			}

		};
	}

}
