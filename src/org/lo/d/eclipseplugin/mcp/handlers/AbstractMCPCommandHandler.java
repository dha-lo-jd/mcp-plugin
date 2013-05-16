package org.lo.d.eclipseplugin.mcp.handlers;

import java.net.URISyntaxException;

import mcp_plugin.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.TextSelection;
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
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemRootNode;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceBinaryNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceSourceNode;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter.ConversionException;
import org.lo.d.eclipseplugin.mcp.process.OSSupport;
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
		final ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (!(selection instanceof IStructuredSelection)) {
			if (selection instanceof TextSelection) {
				System.out.println("TextSelection:" + ((TextSelection) selection).getText());
			}
			throw new ExecutionException("Selection is unknown selection type:" + selection);
		}
		final Object selectionElement = ((IStructuredSelection) selection).getFirstElement();
		if (!(selectionElement instanceof IProject) && !(selectionElement instanceof IJavaProject)) {
			throw new ExecutionException("Selection type is not project.");
		}
		if (selectionElement instanceof IJavaProject) {
			project = ((IJavaProject) selectionElement).getProject();
		} else {
			project = (IProject) selectionElement;
		}
		MessageConsole cosl;
		try {
			cosl = showConsole(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
		} catch (PartInitException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		cosl.clearConsole();

		final NestMessageConsole console = new NestMessageConsole(cosl);
		final MessageConsoleStream out = console.newMessageStream();
		Job job = new Job("MCP build process") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("processing...", 100);

				IProgressMonitor subMonitor;
				try {
					monitor.subTask("loading property.");
					subMonitor = new SubProgressMonitor(monitor, 10);
					subMonitor.beginTask("loading property.", 1);
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					WorkspaceSourceNode workspaceSourceNode = new WorkspaceSourceNode(root);
					WorkspaceBinaryNode workspaceBinaryNode = new WorkspaceBinaryNode(root);
					DirectoryItemRootNode directoryItemRootNode = new DirectoryItemRootNode(root);
					MCPPropertyModel propertyModel = new MCPPropertyModel(Activator.PLUGIN_ID, workspaceSourceNode, workspaceBinaryNode, directoryItemRootNode,
							project);

					propertyModel.setResource(project);
					try {
						propertyModel.load();
					} catch (ConversionException e) {
						throw new ExecutionException(e.getMessage(), e);
					}

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
					subMonitor.worked(1);
					subMonitor.done();

					out.println("Start MCP build process.");

					subMonitor = new SubProgressMonitor(monitor, 90);
					command(console, subMonitor);
					subMonitor.done();

					out.println("End MCP build process.");
				} catch (ExecutionException e) {
					e.printStackTrace(console.newPrintStream());
				}
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					e.printStackTrace(console.newPrintStream());
				}

				monitor.done();

				return Status.OK_STATUS;
			}
		};
		job.setUser(true); // ダイアログを出す
		job.schedule();
		return null;
	}

	public MessageConsole showConsole(IWorkbenchPage page) throws PartInitException {
		IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		MessageConsole console = findConsole(Activator.PLUGIN_ID + ".console");
		view.display(console);

		return console;
	}

	protected abstract void command(NestMessageConsole out, IProgressMonitor monitor) throws ExecutionException;

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, OSSupport.getOSConsoleCharset().name(), true);
		conMan.addConsoles(new IConsole[] {
			myConsole
		});
		return myConsole;
	}

}