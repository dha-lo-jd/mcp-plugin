package org.lo.d.eclipseplugin.mcp.view;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.lo.d.eclipseplugin.mcp.view.AbstractModelListnerNoisyCheckBoxTreeView.NoisyCheckboxTreeViewer;

public abstract class AbstractModelListnerNoisyCheckBoxTreeView extends AbstractModelListnerTreeView<NoisyCheckboxTreeViewer> {

	protected static class NoisyCheckboxTreeViewer extends CheckboxTreeViewer {
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

	public AbstractModelListnerNoisyCheckBoxTreeView(Composite parent, int style, ViwerInitializer<NoisyCheckboxTreeViewer> initializer) {
		super(parent, style, initializer);
	}

	@Override
	protected NoisyCheckboxTreeViewer createViewer() {
		return new NoisyCheckboxTreeViewer(this);
	}

	@Override
	protected NoisyCheckboxTreeViewer initializeViewer() {
		NoisyCheckboxTreeViewer viewer = super.initializeViewer();

		final Tree tree = viewer.getTree();
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
		return viewer;
	}

}