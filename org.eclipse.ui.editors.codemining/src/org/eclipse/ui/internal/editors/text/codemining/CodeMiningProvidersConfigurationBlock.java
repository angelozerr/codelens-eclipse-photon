/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. */
package org.eclipse.ui.internal.editors.text.codemining;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.editors.text.codemining.EditorsUI2;
import org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock;
import org.eclipse.ui.internal.editors.text.OverlayPreferenceStore;
import org.eclipse.ui.texteditor.codemining.CodeMiningDescriptor;
import org.eclipse.ui.texteditor.codemining.CodeMiningProviderDescriptor;
import org.eclipse.ui.texteditor.codemining.CodeMiningProviderTargetDescriptor;

/**
 * Configures hyperlink detector preferences.
 *
 * @since 3.3
 */
class CodeMiningProvidersConfigurationBlock implements IPreferenceConfigurationBlock {

	private static final class ListItem {
		final String id;
		final String name;
		public String availableIn;
		private ListItem[] children;

		public ListItem(String id, String name) {
			this(id, name, null, null);
		}

		public ListItem(String id, String name, String availableIn, ListItem[] children) {
			this.id = id;
			this.name = name;
			this.children = children;
			this.availableIn = availableIn;
		}

		public ListItem[] getChildren() {
			return children;
		}
	}

	private static final class ItemContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (ListItem[]) inputElement;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ListItem) {
				return ((ListItem) parentElement).getChildren();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ListItem) {
				return ((ListItem) element).getChildren() != null;
			}
			return false;
		}
	}

	private final class ItemLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((ListItem) element).name;
			case 1:
				return ((ListItem) element).availableIn;
			default:
				Assert.isLegal(false);
			}
			return null; // cannot happen
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	private OverlayPreferenceStore fStore;
	private PreferencePage fPreferencePage;
	private CheckboxTreeViewer fCodeMiningProviderViewer;
	private CodeMiningProviderDescriptor[] fCodeMiningProviderDescriptors;

	public CodeMiningProvidersConfigurationBlock(PreferencePage preferencePage, OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		Assert.isNotNull(preferencePage);
		fStore = store;
		fPreferencePage = preferencePage;
		fCodeMiningProviderDescriptors = EditorsUI2.getCodeMiningProviderRegistry().getCodeMiningProviderDescriptors();
	}

	@Override
	public Control createControl(Composite parent) {
		PixelConverter pixelConverter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.LEFT);
		label.setText("                                  ");
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		Composite editorComposite = new Composite(composite, SWT.BORDER);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		editorComposite.setLayoutData(gd);

		TreeColumnLayout tableColumnlayout = new TreeColumnLayout();
		editorComposite.setLayout(tableColumnlayout);

		Tree hyperlinkDetectorTable = new Tree(editorComposite,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		hyperlinkDetectorTable.setHeaderVisible(true);
		hyperlinkDetectorTable.setLinesVisible(true);
		hyperlinkDetectorTable.setFont(parent.getFont());

		ColumnLayoutData columnLayoutData = new ColumnWeightData(1);

		TreeColumn nameColumn = new TreeColumn(hyperlinkDetectorTable, SWT.NONE, 0);
		nameColumn.setText(TextEditorMessages2.CodeMiningProviderTree_nameColumn);
		tableColumnlayout.setColumnData(nameColumn, columnLayoutData);

		TreeColumn targetNameColumn = new TreeColumn(hyperlinkDetectorTable, SWT.NONE, 1);
		targetNameColumn.setText(TextEditorMessages2.CodeMiningProviderTree_targetNameColumn);
		tableColumnlayout.setColumnData(targetNameColumn, columnLayoutData);

		fCodeMiningProviderViewer = new CheckboxTreeViewer(hyperlinkDetectorTable);
		fCodeMiningProviderViewer.setLabelProvider(new ItemLabelProvider());
		fCodeMiningProviderViewer.setContentProvider(new ItemContentProvider());
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(20);
		fCodeMiningProviderViewer.getControl().setLayoutData(gd);

		Composite optionsComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		return composite;
	}

	@Override
	public void initialize() {
		ListItem[] listModel = createListModel();
		fCodeMiningProviderViewer.setInput(listModel);
		fCodeMiningProviderViewer.setCheckedElements(getCheckedItems(listModel));
	}

	private Object[] getCheckedItems(ListItem[] listModel) {
		List<ListItem> result = new ArrayList<>();
		for (int i = 0; i < listModel.length; i++)
			if (!fStore.getBoolean(listModel[i].id))
				result.add(listModel[i]);
		return result.toArray();
	}

	@Override
	public void performOk() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canPerformOk() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performDefaults() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyData(Object data) {
		// TODO Auto-generated method stub

	}

	private ListItem[] createListModel() {
		ArrayList<ListItem> listModelItems = new ArrayList<>();
		for (int i = 0; i < fCodeMiningProviderDescriptors.length; i++) {
			CodeMiningProviderDescriptor desc = fCodeMiningProviderDescriptors[i];
			CodeMiningProviderTargetDescriptor target= desc.getTarget();
			
			CodeMiningDescriptor[] minings = desc.getCodeMinigDescriptors();
			List<ListItem> children = new ArrayList<>(minings.length);
			for (CodeMiningDescriptor child : minings) {
				children.add(new ListItem(child.getId(), child.getName()));
			}
			listModelItems.add(new ListItem(desc.getId(), desc.getName(), target.getName(), children.toArray(new ListItem[0])));
		}

		Comparator<ListItem> comparator = new Comparator<ListItem>() {
			@Override
			public int compare(ListItem o1, ListItem o2) {
				String label1 = o1.name;
				String label2 = o2.name;
				return Collator.getInstance().compare(label1, label2);

			}
		};
		Collections.sort(listModelItems, comparator);

		ListItem[] items = new ListItem[listModelItems.size()];
		listModelItems.toArray(items);
		return items;
	}
}
