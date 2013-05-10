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

public class ReobfucateCommand extends AbstractBuildCommand {
	public ReobfucateCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "Reobfucate");
	}

	@Override
	protected void runCommand() throws ExecutionException {
		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve("src/minecraft");

		out.println("Copy src.");
		out.nest();
		{
			for (SourceNode node : property.getProperty().getTargetSrcLocations()) {
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

		MCPCommandSupport.recompile(root, out);
		Path reobfPath = rootPath.resolve("reobf");
		deleteDirectoryAndFiles(reobfPath.toFile());
		createDirectory(reobfPath);
		MCPCommandSupport.reobfuscate(root, out);
	}
}
