package org.lo.d.eclipseplugin.mcp.commands;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole.NestedMessageConsoleStream;
import org.lo.d.eclipseplugin.mcp.process.AsyncProcess;
import org.lo.d.eclipseplugin.mcp.process.OSSupport;

public class MCPCommandSupport {

	public static void recompile(final File root, final NestedMessageConsoleStream out, IProgressMonitor monitor) {
		final String command = "recompile";
		command(root, out, command, "", monitor);
	}

	public static void reobfuscate(final File root, final NestedMessageConsoleStream out, IProgressMonitor monitor) {
		final String command = "reobfuscate";
		command(root, out, command, "", monitor);
	}

	public static void updatemd5(final File root, final NestedMessageConsoleStream out, IProgressMonitor monitor) {
		final String command = "updatemd5";
		command(root, out, command, "yes", monitor);
	}

	private static void command(final File root, final NestedMessageConsoleStream out, final String command, final String in, IProgressMonitor monitor) {
		monitor.setTaskName("exec " + command);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				StringWriter stringWriter = null;
				Writer writer = null;
				try {
					out.println(String.format("try %s.", command));
					String shellCommand = OSSupport.getOSShellCommand();
					writer = new BufferedWriter(new OutputStreamWriter(out.newMessageStream()));
					stringWriter = new StringWriter();
					AsyncProcess.execute(root, in, writer, stringWriter, shellCommand, "/C", command);

					out.println(stringWriter.toString());
					out.println(String.format("finish %s.", command));
				} catch (Exception e) {
				} finally {
					quietClose(writer);
					quietClose(stringWriter);
				}
			}

			private void quietClose(Closeable closeable) {
				if (closeable == null) {
					return;
				}
				try {
					closeable.close();
				} catch (IOException e) {
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
