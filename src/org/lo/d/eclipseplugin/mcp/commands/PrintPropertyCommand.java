package org.lo.d.eclipseplugin.mcp.commands;

import org.eclipse.core.commands.ExecutionException;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand.AbstractBuildCommand;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;

public class PrintPropertyCommand extends AbstractBuildCommand {

	protected PrintPropertyCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "PrintProperty");
	}

	@Override
	protected void runCommand() throws ExecutionException {
		out.println("properties:");
		out.nest();
		printMCPProperty(out);
		out.endNest();
	}

	private void printMCPProperty(NestMessageConsole out) throws ExecutionException {
		out.println("McpLocation: " + property.getMcpLocation());
		out.println("GenerateTempBuildLocation: " + property.getGenerateTempBuildLocation());

		{
			out.println("DependencySrcLocations: [");
			out.nest();
			for (SourceNode sourceNode : property.getProperty().getDependencySrcLocations()) {
				out.indent();
				out.print(sourceNode.getPath());
				out.print(", ");
				out.println();
			}
			out.endNest();
			out.println("]");
		}
		{
			out.println("TargetSrcLocations: [");
			out.nest();
			for (SourceNode sourceNode : property.getProperty().getTargetSrcLocations()) {
				out.indent();
				out.print(sourceNode.getPath());
				out.print(", ");
				out.println();
			}
			out.endNest();
			out.println("]");
		}

		out.println("McpLocation Path: " + property.getMcpLocation());
		out.println("GenerateTempBuildLocation Path: " + property.getGenerateTempBuildLocation());

		monitor.worked(100);
		monitor.done();
	}
}
