package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;

public class ReobfucateOnlyCommand extends ReobfucateCommand {
	private final SetupTempBuildLocationCommand setupTempBuildLocationCommand;

	public ReobfucateOnlyCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out);
		setupTempBuildLocationCommand = new SetupTempBuildLocationCommand(property, out);
	}

	@Override
	public int getCommandCount() {
		return setupTempBuildLocationCommand.getCommandCount() + 1;
	}

	@Override
	protected void runCommand() throws ExecutionException {
		IProgressMonitor subMonitor;
		subMonitor = new SubProgressMonitor(monitor, 50);
		setupTempBuildLocationCommand.run(subMonitor);
		subMonitor.done();
		subMonitor = new SubProgressMonitor(monitor, 50);
		subMonitor.beginTask(name, 150);

		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve("src/minecraft");

		subMonitor.subTask("Cleanup src.");
		out.println("Cleanup src.");
		creanDirectory(srcPath);
		subMonitor.worked(50);

		subMonitor.subTask("Copy src.");
		out.println("Copy src.");
		out.nest();
		{
			for (SourceNode node : property.getProperty().getDependencySrcLocations()) {
				File f = new File(node.getPath());
				if (!f.isDirectory()) {
					continue;
				}
				File[] listFiles = f.listFiles();
				SubProgressMonitor fMon;
				fMon = new SubProgressMonitor(subMonitor, 50);
				fMon.beginTask("Copy src.", listFiles.length);
				for (File file : listFiles) {
					Path entityPath = Paths.get(file.toURI());
					Path linkPath = srcPath.resolve(entityPath.getFileName());

					copyFile(linkPath, entityPath);
					fMon.worked(1);
				}
				fMon.done();
			}
		}
		subMonitor.worked(50);
		out.endNest();

		IProgressMonitor prevMonitor = monitor;
		monitor = new SubProgressMonitor(monitor, 100);
		super.runCommand();
		monitor = prevMonitor;
		subMonitor.worked(50);
		subMonitor.done();
	}
}
