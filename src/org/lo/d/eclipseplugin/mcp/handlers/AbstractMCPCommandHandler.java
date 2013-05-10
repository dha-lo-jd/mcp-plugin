package org.lo.d.eclipseplugin.mcp.handlers;

import java.net.URISyntaxException;

import mcp_plugin.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceNode;
import org.lo.d.eclipseplugin.mcp.resource.support.ProjectPathResolver;

public abstract class AbstractMCPCommandHandler extends AbtractMCPBuildPropertyHandler {

	public AbstractMCPCommandHandler() {
		super();
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			throw new ExecutionException("Selection is unknown selection type.");
		}
		Object selectionElement = ((IStructuredSelection) selection).getFirstElement();
		if (!(selectionElement instanceof IProject) && !(selectionElement instanceof IJavaProject)) {
			throw new ExecutionException("Selection type is not project.");
		}
		if (selectionElement instanceof IJavaProject) {
			project = ((IJavaProject) selectionElement).getProject();
		} else {
			project = (IProject) selectionElement;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		WorkspaceNode workspaceNode = new WorkspaceNode(root);
		MCPPropertyModel propertyModel = new MCPPropertyModel(Activator.PLUGIN_ID, workspaceNode);
		propertyModel.setResource(project);
		propertyModel.load();

		property = propertyModel.getValueAccessor();

		try {
			mcpLocation = ProjectPathResolver.getResolvedFile(property.getMcpLocation(), project);
		} catch (JavaModelException e) {
			throw new ExecutionException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		try {
			generateTempBuildLocation = ProjectPathResolver.getResolvedFile(property.getGenerateTempBuildLocation(), project);
		} catch (JavaModelException e) {
			throw new ExecutionException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new ExecutionException(e.getMessage(), e);
		}

		MessageConsole console;
		try {
			console = showConsole(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
		} catch (PartInitException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		console.clearConsole();

		NestMessageConsole out = new NestMessageConsole(console.newMessageStream());
		System.out.println(console.getEncoding());

		out.println("Start MCP build process.");

		command(out);

		out.println("End MCP build process.");
		return null;
	}

	public MessageConsole showConsole(IWorkbenchPage page) throws PartInitException {
		IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		MessageConsole console = findConsole(Activator.PLUGIN_ID + ".console");
		view.display(console);

		return console;
	}

	protected abstract void command(NestMessageConsole out) throws ExecutionException;

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, "MS932", true);
		conMan.addConsoles(new IConsole[] {
			myConsole
		});
		return myConsole;
	}

}