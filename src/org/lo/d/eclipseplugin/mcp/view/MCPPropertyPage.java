package org.lo.d.eclipseplugin.mcp.view;

import mcp_plugin.Activator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceNode;

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
		propertyModel.save();
		return true;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		WorkspaceNode workspaceNode = new WorkspaceNode(root);

		initModel(workspaceNode);

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

				tabItemMcpSettings.setControl(new MCPLocationView(tabFolder,
						SWT.NONE, (IProject) getElement(), propertyModel
								.getMcpLocation(), propertyModel
								.getGenerateTempBuildLocation()));

				tabFolder.setSelection(tabItemMcpSettings);
			}

			{
				CTabItem tabItemDependencySrcSettings = new CTabItem(tabFolder,
						SWT.NONE);
				tabItemDependencySrcSettings.setText("依存ソース・ロケーション");
				tabItemDependencySrcSettings
						.setControl(new SourceLocationTreeView(tabFolder,
								SWT.NONE, workspaceNode, propertyModel
										.getDependencySrcLocations()));
			}
			{
				CTabItem tabItemTargetSrcSettings = new CTabItem(tabFolder,
						SWT.NONE);
				tabItemTargetSrcSettings.setText("対象ソース・ロケーション");
				tabItemTargetSrcSettings.setControl(new SourceLocationTreeView(
						tabFolder, SWT.NONE, workspaceNode, propertyModel
								.getTargetSrcLocations()));
			}
		}

		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		propertyModel.loadDefault();
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

	private void initModel(WorkspaceNode workspaceNode) {
		propertyModel = new MCPPropertyModel(Activator.PLUGIN_ID, workspaceNode);
		propertyModel.setResource((IResource) getElement());
		propertyModel.load();
	}

}