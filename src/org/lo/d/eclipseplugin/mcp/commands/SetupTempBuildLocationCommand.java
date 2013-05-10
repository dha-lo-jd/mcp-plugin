package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionException;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand.AbstractBuildCommand;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;

public class SetupTempBuildLocationCommand extends AbstractBuildCommand {
	private final PrintPropertyCommand printMessageCommand;

	public SetupTempBuildLocationCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "SetupTempBuildLocation");
		printMessageCommand = new PrintPropertyCommand(property, out);
	}

	@Override
	protected void runCommand() throws ExecutionException {
		printMessageCommand.run();
		File mcpDir = property.getMcpLocation();

		if (!mcpDir.exists()) {
			throw new ExecutionException(mcpDir + " is not found.");
		}

		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve("src/minecraft");

		out.println("Gen dirs.");
		out.nest();
		createDirectory(rootPath);
		createDirectory(srcPath);
		out.endNest();

		out.println("Gen links.");
		out.nest();
		generateLinks(mcpDir, rootPath);
		out.endNest();
	}

}
