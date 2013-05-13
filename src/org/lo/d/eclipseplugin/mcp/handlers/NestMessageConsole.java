package org.lo.d.eclipseplugin.mcp.handlers;

import java.io.PrintStream;

import org.eclipse.ui.console.MessageConsoleStream;

public class NestMessageConsole extends PrintStream {
	private final MessageConsoleStream out;

	private int level = 0;

	public NestMessageConsole(MessageConsoleStream out) {
		super(out);
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

	@Override
	public void print(String message) {
		out.print(message);
	}

	@Override
	public void println() {
		out.println();
	}

	@Override
	public void println(String message) {
		indent();
		out.println(message);
	}
}
