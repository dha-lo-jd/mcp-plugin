package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand;
import org.lo.d.eclipseplugin.mcp.commands.ReobfucateCommand;
import org.lo.d.eclipseplugin.mcp.commands.UpdateMD5Command;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class UpdateMD5AndReobfucateCommandHandler extends AbstractMCPCommandHandler {
	/**
	 * The constructor.
	 */
	public UpdateMD5AndReobfucateCommandHandler() {
	}

	@Override
	protected void command(NestMessageConsole out, IProgressMonitor monitor) throws ExecutionException {
		monitor.beginTask("", 200);
		{
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
			BuildCommand command = new UpdateMD5Command(this, out);
			command.run(subMonitor);
			subMonitor.done();
		}
		{
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
			BuildCommand command = new ReobfucateCommand(this, out);
			command.run(subMonitor);
			subMonitor.done();
		}
	}
}
