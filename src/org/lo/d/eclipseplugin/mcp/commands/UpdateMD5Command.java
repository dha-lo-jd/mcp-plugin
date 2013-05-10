package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionException;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand.AbstractBuildCommand;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.MCPCommandSupport;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;

public class UpdateMD5Command extends AbstractBuildCommand {
	private final SetupTempBuildLocationCommand setupTempBuildLocationCommand;

	public UpdateMD5Command(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "UpdateMD5");
		setupTempBuildLocationCommand = new SetupTempBuildLocationCommand(property, out);
	}

	@Override
	protected void runCommand() throws ExecutionException {
		setupTempBuildLocationCommand.run();

		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve("src/minecraft");

		out.println("Cleanup src.");
		creanDirectory(srcPath);

		out.println("Copy src.");
		out.nest();
		{
			for (SourceNode node : property.getProperty().getDependencySrcLocations()) {
				File f = new File(node.getPath());
				if (!f.isDirectory()) {
					continue;
				}
				for (File file : f.listFiles()) {
					Path entityPath = Paths.get(file.toURI());
					Path linkPath = srcPath.resolve(entityPath.getFileName());

					copyFile(linkPath, entityPath);
				}
			}
		}
		out.endNest();

		MCPCommandSupport.updatemd5(root, out);
	}
}
