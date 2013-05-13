package org.lo.d.eclipseplugin.mcp.commands;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.process.AsyncProcess;
import org.lo.d.eclipseplugin.mcp.process.OSSupport;

public class MCPCommandSupport {

	public static void recompile(final File root, final NestMessageConsole out, IProgressMonitor monitor) {
		final String command = "recompile";
		command(root, out, command, "", monitor);
	}

	public static void reobfuscate(final File root, final NestMessageConsole out, IProgressMonitor monitor) {
		final String command = "reobfuscate";
		command(root, out, command, "", monitor);
	}

	public static void updatemd5(final File root, final NestMessageConsole out, IProgressMonitor monitor) {
		final String command = "updatemd5";
		command(root, out, command, "yes", monitor);
	}

	private static void command(final File root, final NestMessageConsole out, final String command, final String in, IProgressMonitor monitor) {
		monitor.setTaskName("exec " + command);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					out.println(String.format("try %s.", command));
					String shellCommand = OSSupport.getOSShellCommand();
					OutputStreamWriter writer = new OutputStreamWriter(out.getOut());
					StringWriter stringWriter = new StringWriter();
					AsyncProcess.execute(root, in, writer, stringWriter, shellCommand, "/C", command);

					out.println(stringWriter.toString());
					out.println(String.format("finish %s.", command));
				} catch (Exception e) {
				}
			}
		});
		thread.start();

		int i = 0;
		while (thread.isAlive()) {
			try {
				Thread.sleep(250);
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < i; j++) {
					sb.append(".");
				}
				monitor.subTask("processing" + sb.toString());
				i++;
			} catch (InterruptedException e) {
			}
		}
	}
}
