package org.lo.d.eclipseplugin.mcp.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.lo.d.eclipseplugin.mcp.model.AbstractNodeTree.Node;

public abstract class AbstractModelListnerTreeView<TV extends TreeViewer> extends Composite {

	public interface ViwerInitializer<TV extends TreeViewer> {
		public void initialize(TV viewer);
	}

	protected static abstract class AbstractCheckStateListener<CTV extends CheckboxTreeViewer> implements ICheckStateListener {
		protected final CTV viewer;

		protected AbstractCheckStateListener(CTV viewer) {
			this.viewer = viewer;
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Node node = (Node) event.getElement();
			boolean checked = event.getChecked();
			setChecked(node, checked);

			checkAndTraverseSubTree(node, checked);
			notifyCheckStateChangedParent(node);
		}

		protected abstract void setChecked(Node node, boolean checked);

		private void checkAndTraverseSubTree(Node node, boolean state) {
			viewer.setGrayed(node, false);
			Node[] children = node.getChildren();
			if (children == null) {
				return;
			}
			for (Node child : children) {
				setChecked(child, state);
				checkAndTraverseSubTree(child, state);
			}
		}

		private void notifyCheckStateChangedParent(Node node) {
			Node parent = node.getParent();
			if (parent == null) {
				return;
			}

			boolean isAllChecked;
			isAllChecked = true;
			boolean isAnyoneChecked;
			isAnyoneChecked = false;
			for (Node child : parent.getChildren()) {
				if (viewer.getChecked(child)) {
					isAnyoneChecked = true;
				}
				if (!viewer.getChecked(child) || viewer.getGrayed(child)) {
					isAllChecked = false;
				}
			}
			viewer.setGrayed(parent, !isAllChecked && isAnyoneChecked);
			setChecked(parent, isAnyoneChecked);

			notifyCheckStateChangedParent(parent);
		}
	}

	private class ContentProvidor implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (!(parentElement instanceof Node)) {
				return null;
			}
			return ((Node) parentElement).getChildren();
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object getParent(Object element) {
			if (!(element instanceof Node)) {
				return null;
			}
			return ((Node) element).getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			if (!(element instanceof Node)) {
				return false;
			}
			return ((Node) element).hasChildren();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class LabelProvidor extends StyledCellLabelProvider {
		// The listeners
		private List<ILabelProviderListener> listeners;

		private LabelProvidor() {
			super();
			listeners = new ArrayList<ILabelProviderListener>();
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
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
			listeners.remove(listener);
		}

		@Override
		public void update(ViewerCell cell) {
			super.update(cell);
			if (!(cell.getElement() instanceof Node)) {
				cell.setText("error : " + cell.getElement());
				return;
			}
			Node node = (Node) cell.getElement();

			StyledString styledString = new StyledString();
			node.buildLabel(styledString);

			cell.setText(styledString.toString());
			cell.setStyleRanges(styledString.getStyleRanges());

			cell.setImage(node.getImage());
		}

	}

	protected final TV viewer;

	public AbstractModelListnerTreeView(Composite parent, int style, ViwerInitializer<TV> initializer) {
		super(parent, style);
		init();

		setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = initializeViewer();

		initializer.initialize(viewer);
	}

	protected abstract TV createViewer();

	protected void init() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		setLayout(layout);
		Display display = Display.getCurrent();
		setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		setLayoutData(data);
	}

	protected TV initializeViewer() {
		TV viewer = createViewer();

		final Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.setLabelProvider(new LabelProvidor());
		viewer.setContentProvider(new ContentProvidor());
		return viewer;
	}

}