package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubProgressMonitor;
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
	public int getCommandCount() {
		return printMessageCommand.getCommandCount() + 1;
	}

	@Override
	protected void runCommand() throws ExecutionException {
		SubProgressMonitor subMonitor;
		subMonitor = new SubProgressMonitor(monitor, 50);
		printMessageCommand.run(subMonitor);
		subMonitor.done();
		subMonitor = new SubProgressMonitor(monitor, 50);
		subMonitor.beginTask(name, 100);
		File mcpDir = property.getMcpLocation();

		if (!mcpDir.exists()) {
			throw new ExecutionException(mcpDir + " is not found.");
		}

		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path srcPath = rootPath.resolve(MCP_DIR_SRC);
		Path binPath = rootPath.resolve(MCP_DIR_BIN);

		subMonitor.subTask("Gen dirs.");
		out.println("Gen dirs.");
		out.nest();
		createDirectory(rootPath);
		createDirectory(srcPath);
		createDirectory(binPath);
		out.endNest();
		subMonitor.worked(50);

		subMonitor.subTask("Gen links.");
		out.println("Gen links.");
		out.nest();
		generateLinks(mcpDir, rootPath);
		out.endNest();
		subMonitor.worked(50);
		subMonitor.done();
	}

}
