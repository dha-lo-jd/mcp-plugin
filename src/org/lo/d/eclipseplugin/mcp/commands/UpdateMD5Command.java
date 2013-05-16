package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand.AbstractBuildCommand;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;

public class UpdateMD5Command extends AbstractBuildCommand {
	private final SetupTempBuildLocationCommand setupTempBuildLocationCommand;

	public UpdateMD5Command(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "UpdateMD5");
		setupTempBuildLocationCommand = new SetupTempBuildLocationCommand(property, out);
	}

	@Override
	public int getCommandCount() {
		return setupTempBuildLocationCommand.getCommandCount() + 1;
	}

	@Override
	protected void runCommand() throws ExecutionException {
		{
			SubProgressMonitor subMonitor;
			subMonitor = new SubProgressMonitor(monitor, 10);
			setupTempBuildLocationCommand.run(subMonitor);
			subMonitor.done();
		}

		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve(MCP_DIR_SRC);

		monitor.setTaskName("Cleanup src.");
		out.println("Cleanup src.");
		creanDirectory(srcPath);
		monitor.worked(30);

		monitor.setTaskName("Copy src.");
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
				fMon = new SubProgressMonitor(monitor, 30);
				fMon.beginTask("Copy src.", listFiles.length);
				for (File file : listFiles) {
					Path entityPath = Paths.get(file.toURI());
					Path linkPath = srcPath.resolve(entityPath.getFileName());

					fMon.subTask(linkPath.toString());
					copyFile(linkPath, entityPath, "java");
					fMon.worked(1);
				}
				fMon.done();
			}
		}
		out.endNest();

		monitor.setTaskName(name);
		MCPCommandSupport.updatemd5(root, out, monitor);
		monitor.worked(30);

		monitor.done();
	}
}
