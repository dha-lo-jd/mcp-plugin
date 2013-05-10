package org.lo.d.eclipseplugin.mcp.handlers;

import org.eclipse.ui.console.MessageConsoleStream;

public class NestMessageConsole {
	private final MessageConsoleStream out;
	private int level = 0;

	public NestMessageConsole(MessageConsoleStream out) {
		this.out = out;
	}

	public void endNest() {
		if (level <= 0) {
			level = 0;
			return;
		}
		level--;
	}

	public MessageConsoleStream getOut() {
		return out;
	}

	public void indent() {
		for (int i = 0; i < level; i++) {
			out.print("\t");
		}
	}

	public void nest() {
		level++;
	}

	public void print(String message) {
		out.print(message);
	}

	public void println() {
		out.println();
	}

	public void println(String message) {
		indent();
		out.println(message);
	}
}
