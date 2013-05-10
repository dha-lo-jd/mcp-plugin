package org.lo.d.eclipseplugin.mcp.handlers;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.lo.d.eclipseplugin.mcp.process.AsyncProcess;
import org.lo.d.eclipseplugin.mcp.process.OSSupport;

public class MCPCommandSupport {

	public static void recompile(final File root, final NestMessageConsole out) {
		final String command = "recompile";
		command(root, out, command, "");
	}

	public static void reobfuscate(final File root, final NestMessageConsole out) {
		final String command = "reobfuscate";
		command(root, out, command, "");
	}

	public static void updatemd5(final File root, final NestMessageConsole out) {
		final String command = "updatemd5";
		command(root, out, command, "yes\n");
	}

	private static void command(final File root, final NestMessageConsole out, final String command, final String in) {
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

		System.out.print("wait");
		while (thread.isAlive()) {
			try {
				Thread.sleep(1000);
				System.out.print(".");
			} catch (InterruptedException e) {
			}
		}
		System.out.println();
	}
}
