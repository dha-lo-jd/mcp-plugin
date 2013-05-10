package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand;
import org.lo.d.eclipseplugin.mcp.commands.CompressCommand;
import org.lo.d.eclipseplugin.mcp.commands.ReobfucateOnlyCommand;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ReobfOnlyCommandHandler extends AbstractMCPCommandHandler {
	/**
	 * The constructor.
	 */
	public ReobfOnlyCommandHandler() {
	}

	@Override
	protected void command(NestMessageConsole out) throws ExecutionException {
		{
			BuildCommand command = new ReobfucateOnlyCommand(this, out);
			command.run();
		}
		{
			BuildCommand command = new CompressCommand(this, out);
			command.run();
		}
	}

}
