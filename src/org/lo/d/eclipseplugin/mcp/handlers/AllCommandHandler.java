package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand;
import org.lo.d.eclipseplugin.mcp.commands.CompressCommand;
import org.lo.d.eclipseplugin.mcp.commands.ReobfucateCommand;
import org.lo.d.eclipseplugin.mcp.commands.UpdateMD5Command;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AllCommandHandler extends AbstractMCPCommandHandler {
	/**
	 * The constructor.
	 */
	public AllCommandHandler() {
	}

	@Override
	protected void command(final NestMessageConsole out, IProgressMonitor monitor) throws ExecutionException {
		monitor.beginTask("", 300);
		try {
			{
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
				BuildCommand command = new UpdateMD5Command(AllCommandHandler.this, out);
				command.run(subMonitor);
				subMonitor.done();
			}
			{
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
				BuildCommand command = new ReobfucateCommand(AllCommandHandler.this, out);
				command.run(subMonitor);
				subMonitor.done();
			}
			{
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
				BuildCommand command = new CompressCommand(AllCommandHandler.this, out);
				command.run(subMonitor);
				subMonitor.done();
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
