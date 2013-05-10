package org.lo.d.eclipseplugin.mcp.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ListenerProperty;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ValueReceiver;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.Node;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceNode;

public class SourceLocationTreeView extends Composite {

	private class CheckStateListener implements ICheckStateListener {
		private final CheckboxTreeViewer viewer;
		private final ListenerProperty<SourceNode> listenerProperty;

		private CheckStateListener(CheckboxTreeViewer viewer,
				ListenerProperty<SourceNode> listenerProperty) {
			this.viewer = viewer;
			this.listenerProperty = listenerProperty;
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Node node = (Node) event.getElement();
			boolean checked = event.getChecked();
			setChecked(node, checked);

			checkAndTraverseSubTree(node, checked);
			notifyCheckStateChangedParent(node);
		}

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

		private void setChecked(Node node, boolean state) {
			viewer.setChecked(node, state);
			if (node instanceof SourceNode) {
				SourceNode sourceNode = (SourceNode) node;
				if (state) {
					listenerProperty.add(sourceNode);
				} else {
					listenerProperty.remove(sourceNode);
				}
			}
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

	private class LabelProvidor implements ILabelProvider {
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
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (!(element instanceof Node)) {
				return null;
			}
			return ((Node) element).getName();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

	}

	private static class NoisyCheckboxTreeViewer extends CheckboxTreeViewer {
		private NoisyCheckboxTreeViewer(Composite parent) {
			super(parent);
		}

		private NoisyCheckboxTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		private NoisyCheckboxTreeViewer(Tree tree) {
			super(tree);
		}

		public void notifyCheckStateChanged(CheckStateChangedEvent event) {
			super.fireCheckStateChanged(event);
		}
	}

	private class Reciever implements ValueReceiver<SourceNode> {
		private final NoisyCheckboxTreeViewer viewer;

		private Reciever(NoisyCheckboxTreeViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void add(SourceNode value) {
			fire(value, true);
		}

		@Override
		public void remove(SourceNode value) {
			fire(value, false);
		}

		private void fire(SourceNode value, boolean state) {
			if (viewer.getChecked(value) == state) {
				return;
			}
			viewer.setChecked(value, state);
			viewer.notifyCheckStateChanged(new CheckStateChangedEvent(viewer,
					value, state));
		}
	}

	public SourceLocationTreeView(Composite parent, int style,
			WorkspaceNode workspaceNode,
			ListenerProperty<SourceNode> listenerProperty) {
		super(parent, style);

		init();

		setLayoutData(new GridData(GridData.FILL_BOTH));

		NoisyCheckboxTreeViewer viewer = new NoisyCheckboxTreeViewer(this);
		viewer.addCheckStateListener(new CheckStateListener(viewer,
				listenerProperty));

		final Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				TreeItem item = tree.getItem(point);
				if (item == null) {
					return;
				}
				if (item.getBounds().contains(point)) {
					boolean checked = !item.getChecked();
					item.setChecked(checked);
				}
			}
		});

		viewer.setLabelProvider(new LabelProvidor());
		viewer.setContentProvider(new ContentProvidor());

		viewer.setInput(workspaceNode);

		listenerProperty.addReceiver(new Reciever(viewer));
	}

	private void init() {
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

}
