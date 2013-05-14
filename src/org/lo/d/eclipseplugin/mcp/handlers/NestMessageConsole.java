package org.lo.d.eclipseplugin.mcp.handlers;

import java.io.PrintStream;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class NestMessageConsole {
	public static class NestedMessageConsoleStream extends MessageConsoleStream {
		private final MessageConsoleStream out;

		private int parentLevel = 0;

		private int level = 0;

		private NestedMessageConsoleStream(MessageConsole console, MessageConsoleStream out) {
			super(console);
			this.out = out;
		}

		private NestedMessageConsoleStream(MessageConsole console, MessageConsoleStream out, int parentLevel) {
			super(console);
			this.out = out;
			this.parentLevel = parentLevel;
		}

		public void endNest() {
			if (level <= 0) {
				level = 0;
				return;
			}
			level--;
		}

		public void indent() {
			for (int i = 0; i < level + parentLevel; i++) {
				out.print("\t");
			}
		}

		public void nest() {
			level++;
		}

		public MessageConsoleStream newMessageStream() {
			return new NestedMessageConsoleStream(out.getConsole(), out.getConsole().newMessageStream(), level);
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

	private static class PStream extends PrintStream {
		private final MessageConsoleStream out;

		private int parentLevel = 0;

		private int level = 0;

		private PStream(MessageConsole console, MessageConsoleStream out) {
			super(out);
			this.out = out;
		}

		private PStream(MessageConsole console, MessageConsoleStream out, int parentLevel) {
			super(out);
			this.out = out;
			this.parentLevel = parentLevel;
		}

		public void endNest() {
			if (level <= 0) {
				level = 0;
				return;
			}
			level--;
		}

		public void indent() {
			for (int i = 0; i < level + parentLevel; i++) {
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

	private final MessageConsole console;

	public NestMessageConsole(MessageConsole console) {
		this.console = console;
	}

	public MessageConsole getConsole() {
		return console;
	}

	public NestedMessageConsoleStream newMessageStream() {
		return new NestedMessageConsoleStream(console, console.newMessageStream());
	}

	public NestedMessageConsoleStream newOutputStream() {
		return new NestedMessageConsoleStream(console, console.newMessageStream());
	}

	public PrintStream newPrintStream() {
		return new PStream(console, console.newMessageStream());
	}
}
