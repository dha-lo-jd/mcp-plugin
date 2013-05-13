package org.lo.d.eclipseplugin.mcp.view;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.lo.d.eclipseplugin.mcp.model.AbstractNodeTree.Node;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ListenerProperty;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ValueReceiver;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceNode;

public class SourceLocationTreeView extends AbstractModelListnerNoisyCheckBoxTreeView {
	protected static class CheckStateListener extends AbstractCheckStateListener {
		private final ListenerProperty<SourceNode> listenerProperty;

		protected CheckStateListener(CheckboxTreeViewer viewer, ListenerProperty<SourceNode> listenerProperty) {
			super(viewer);
			this.listenerProperty = listenerProperty;
		}

		@Override
		protected void setChecked(Node node, boolean state) {
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

	private static class Initializer implements ViwerInitializer<NoisyCheckboxTreeViewer> {
		private ListenerProperty<SourceNode> listenerProperty;
		private WorkspaceNode workspaceNode;

		private Initializer(ListenerProperty<SourceNode> listenerProperty, WorkspaceNode workspaceNode) {
			super();
			this.listenerProperty = listenerProperty;
			this.workspaceNode = workspaceNode;
		}

		@Override
		public void initialize(NoisyCheckboxTreeViewer viewer) {
			viewer.addCheckStateListener(new CheckStateListener(viewer, listenerProperty));
			viewer.setInput(workspaceNode);

			listenerProperty.addReceiver(new Reciever(viewer));
		}
	}

	private static class Reciever implements ValueReceiver<SourceNode> {
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
			if (value == null) {
				return;
			}
			if (viewer.getChecked(value) == state) {
				return;
			}
			viewer.setChecked(value, state);
			viewer.notifyCheckStateChanged(new CheckStateChangedEvent(viewer, value, state));
		}
	}

	public SourceLocationTreeView(Composite parent, int style, WorkspaceNode workspaceNode, ListenerProperty<SourceNode> listenerProperty) {
		super(parent, style, new Initializer(listenerProperty, workspaceNode));
	}

}
