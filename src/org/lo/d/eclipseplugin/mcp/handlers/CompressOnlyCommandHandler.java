package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand;
import org.lo.d.eclipseplugin.mcp.commands.CompressCommand;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CompressOnlyCommandHandler extends AbstractMCPCommandHandler {
	/**
	 * The constructor.
	 */
	public CompressOnlyCommandHandler() {
	}

	@Override
	protected void command(NestMessageConsole out) throws ExecutionException {
		{
			BuildCommand command = new CompressCommand(this, out);
			command.run();
		}
	}

}
