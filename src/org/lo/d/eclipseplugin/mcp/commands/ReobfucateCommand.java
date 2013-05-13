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

public class ReobfucateCommand extends AbstractBuildCommand {
	public ReobfucateCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "Reobfucate");
	}

	@Override
	protected void runCommand() throws ExecutionException {
		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve("src/minecraft");

		monitor.setTaskName("Copy src.");
		out.println("Copy src.");
		out.nest();
		{
			for (SourceNode node : property.getProperty().getTargetSrcLocations()) {
				File f = new File(node.getPath());
				if (!f.isDirectory()) {
					continue;
				}
				File[] listFiles = f.listFiles();
				SubProgressMonitor fMon;
				fMon = new SubProgressMonitor(monitor, 20);
				fMon.beginTask("Copy src.", listFiles.length);
				for (File file : listFiles) {
					Path entityPath = Paths.get(file.toURI());
					Path linkPath = srcPath.resolve(entityPath.getFileName());

					fMon.subTask(linkPath.toString());
					copyFile(linkPath, entityPath);
					fMon.worked(1);
				}
				fMon.done();
			}
		}
		out.endNest();
		monitor.worked(20);

		MCPCommandSupport.recompile(root, out, monitor);
		monitor.worked(20);
		Path reobfPath = rootPath.resolve("reobf");
		deleteDirectoryAndFiles(reobfPath.toFile());
		createDirectory(reobfPath);
		monitor.worked(20);
		MCPCommandSupport.reobfuscate(root, out, monitor);
		monitor.worked(20);

		monitor.done();
	}
}
