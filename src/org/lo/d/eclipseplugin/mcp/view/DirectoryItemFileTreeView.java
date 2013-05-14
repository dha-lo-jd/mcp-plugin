package org.lo.d.eclipseplugin.mcp.view;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ListenerProperty;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ValueReceiver;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemNode;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemRootNode;

public class DirectoryItemFileTreeView extends AbstractModelListnerTreeView<TreeViewer> {
	private static class Initializer implements ViwerInitializer<TreeViewer> {
		private ListenerProperty<DirectoryItemNode> listenerProperty;
		private DirectoryItemRootNode workspaceNode;

		private Initializer(ListenerProperty<DirectoryItemNode> listenerProperty, DirectoryItemRootNode workspaceNode) {
			super();
			this.listenerProperty = listenerProperty;
			this.workspaceNode = workspaceNode;
		}

		@Override
		public void initialize(TreeViewer viewer) {
			viewer.setInput(workspaceNode);
			listenerProperty.addReceiver(new Reciever(viewer, workspaceNode));

		}
	}

	private static class Reciever implements ValueReceiver<DirectoryItemNode> {
		private final TreeViewer viewer;
		private final DirectoryItemRootNode workspaceNode;

		private Reciever(TreeViewer viewer, DirectoryItemRootNode workspaceNode) {
			this.viewer = viewer;
			this.workspaceNode = workspaceNode;
		}

		@Override
		public void add(DirectoryItemNode value) {
			workspaceNode.add(value);
			viewer.refresh();
		}

		@Override
		public void remove(DirectoryItemNode value) {
			workspaceNode.remove(value);
			viewer.refresh();
		}

	}

	public DirectoryItemFileTreeView(Composite parent, int style, final DirectoryItemRootNode workspaceNode,
			ListenerProperty<DirectoryItemNode> listenerProperty, Path rootLacation) {
		super(parent, style, new Initializer(listenerProperty, workspaceNode));

		setupButtons(workspaceNode, listenerProperty, rootLacation);
	}

	@Override
	protected TreeViewer createViewer() {
		return new TreeViewer(this);
	}

	@Override
	protected void init() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
		Display display = Display.getCurrent();
		setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		setLayoutData(data);
	}

	private void setupButtons(final DirectoryItemRootNode workspaceNode, final ListenerProperty<DirectoryItemNode> listenerProperty, final Path rootLacation) {
		Composite buttons = new Composite(this, SWT.TOP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		buttons.setLayout(layout);
		Display display = Display.getCurrent();
		buttons.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		buttons.setLayoutData(data);

		GridData buttonData = new GridData();
		buttonData.widthHint = 150;
		Button addFolderButton = new Button(buttons, SWT.NONE);
		addFolderButton.setLayoutData(buttonData);
		addFolderButton.setText("フォルダ追加");
		addFolderButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setText("フォルダの選択");
				dialog.setMessage("リソース・ロケーションに追加");
				dialog.setFilterPath(rootLacation.toString());
				String path = dialog.open();

				if (path != null && !path.isEmpty()) {
					listenerProperty.add(new DirectoryItemNode(workspaceNode, new File(path)));
				}
			}
		});
		Button addFileButton = new Button(buttons, SWT.NONE);
		addFileButton.setLayoutData(buttonData);
		addFileButton.setText("ファイル追加");
		addFileButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell);
				dialog.setText("ファイルの選択");
				dialog.setFilterPath(rootLacation.toString());
				String path = dialog.open();

				if (path != null && !path.isEmpty()) {
					listenerProperty.add(new DirectoryItemNode(workspaceNode, new File(path)));
				}
			}
		});

		final Button deleteButton = new Button(buttons, SWT.NONE);
		deleteButton.setLayoutData(buttonData);
		deleteButton.setText("除去");
		deleteButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ISelection selection = viewer.getSelection();
				if (!(selection instanceof IStructuredSelection)) {
					return;
				}

				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (!(element instanceof DirectoryItemNode)) {
					return;
				}

				listenerProperty.remove((DirectoryItemNode) element);
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				deleteButton.setEnabled(event.getSelection() != null);
			}
		});
	}

}
