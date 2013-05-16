package org.lo.d.eclipseplugin.mcp.view;

import mcp_plugin.Activator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PropertyPage;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemRootNode;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceBinaryNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceSourceNode;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter.ConversionException;

public class MCPPropertyPage extends PropertyPage {

	private MCPPropertyModel propertyModel;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public MCPPropertyPage() {
		super();
	}

	@Override
	public boolean performOk() {
		try {
			propertyModel.save();
		} catch (ConversionException e) {
			new RuntimeException(e);
		}
		return true;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		WorkspaceSourceNode workspaceSourceNode = new WorkspaceSourceNode(root);
		WorkspaceBinaryNode workspaceBinaryNode = new WorkspaceBinaryNode(root);
		DirectoryItemRootNode directoryItemRootNode = new DirectoryItemRootNode(root);

		IProject project = getProject();
		try {
			initModel(workspaceSourceNode, workspaceBinaryNode, directoryItemRootNode);
		} catch (ConversionException e) {
			throw new RuntimeException(e);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		{
			CTabFolder tabFolder = new CTabFolder(composite, SWT.BORDER);
			tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

			{
				CTabItem tabItemMcpSettings = new CTabItem(tabFolder, SWT.NONE);
				tabItemMcpSettings.setText("MCPロケーション");

				tabItemMcpSettings.setControl(new MCPLocationView(tabFolder, SWT.NONE, project, propertyModel));

				tabFolder.setSelection(tabItemMcpSettings);
			}

			{
				CTabItem tabItemDependencySrcSettings = new CTabItem(tabFolder, SWT.NONE);
				tabItemDependencySrcSettings.setText("MD5生成ソース・ロケーション");
				tabItemDependencySrcSettings.setControl(new SourceLocationTreeView(tabFolder, SWT.NONE, workspaceSourceNode, propertyModel
						.getDependencySrcLocations()));
			}
			{
				CTabItem tabItemTargetSrcSettings = new CTabItem(tabFolder, SWT.NONE);
				tabItemTargetSrcSettings.setText("難読化対象クラス・ロケーション");
				tabItemTargetSrcSettings
						.setControl(new SourceLocationTreeView(tabFolder, SWT.NONE, workspaceBinaryNode, propertyModel.getTargetSrcLocations()));
			}
			{
				CTabItem tabItemTargetSrcSettings = new CTabItem(tabFolder, SWT.NONE);
				tabItemTargetSrcSettings.setText("リソース・ロケーション");
				tabItemTargetSrcSettings.setControl(new DirectoryItemFileTreeView(tabFolder, SWT.NONE, directoryItemRootNode, propertyModel
						.getResourceLocations(), project.getLocation().toFile().toPath()));
			}
		}

		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		try {
			propertyModel.loadDefault();
		} catch (ConversionException e) {
			new RuntimeException(e);
		}
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		Display display = Display.getCurrent();
		composite.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	private IProject getProject() {
		final IAdaptable o = getElement();
		if (o instanceof IJavaProject) {
			return ((IJavaProject) o).getProject();
		} else if (o instanceof IProject) {
			return ((IProject) o).getProject();
		} else {
			throw new IllegalArgumentException(getElement().toString());
		}
	}

	private void initModel(WorkspaceSourceNode workspaceSourceNode, WorkspaceBinaryNode workspaceBinaryNode, DirectoryItemRootNode directoryItemRootNode)
			throws ConversionException {
		propertyModel = new MCPPropertyModel(Activator.PLUGIN_ID, workspaceSourceNode, workspaceBinaryNode, directoryItemRootNode, getProject());
		propertyModel.setResource(getProject());
		propertyModel.load();
	}

}