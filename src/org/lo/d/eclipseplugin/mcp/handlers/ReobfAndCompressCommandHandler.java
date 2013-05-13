package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand;
import org.lo.d.eclipseplugin.mcp.commands.CompressCommand;
import org.lo.d.eclipseplugin.mcp.commands.ReobfucateOnlyCommand;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ReobfAndCompressCommandHandler extends AbstractMCPCommandHandler {
	/**
	 * The constructor.
	 */
	public ReobfAndCompressCommandHandler() {
	}

	@Override
	protected void command(NestMessageConsole out, IProgressMonitor monitor) throws ExecutionException {
		monitor.beginTask("", 200);
		{
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
			BuildCommand command = new ReobfucateOnlyCommand(this, out);
			command.run(subMonitor);
			subMonitor.done();
		}
		{
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
			BuildCommand command = new CompressCommand(this, out);
			command.run(subMonitor);
			subMonitor.done();
		}
	}

}
