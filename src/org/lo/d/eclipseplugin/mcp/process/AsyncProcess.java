package org.lo.d.eclipseplugin.mcp.process;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 標準入力、標準出力、標準エラーをそれぞれ個々のスレッドで読み書きする
 */
public class AsyncProcess {

	/**
	 * 指定されたInputStreamからOutputStreamに出力を行うだけのクラス
	 */
	public static class StreamPipeThread extends Thread {

		protected Writer out;
		protected Reader in;

		protected boolean close = false;

		public StreamPipeThread(Reader in, Writer out) {
			super();
			this.out = out;
			this.in = in;
		}

		public StreamPipeThread(Reader in, Writer out, boolean close) {
			this(in, out);
			this.close = close;
		}

		@Override
		public void run() {
			super.run();

			int b;
			try {
				if (in != null && out != null) {
					int intervalFlush = 100;
					while ((b = in.read()) != -1) {
						out.write(b);
						intervalFlush--;
						if (intervalFlush <= 0) {
							intervalFlush = 100;
							out.flush();
						}
					}
					out.flush();
				} else if (in != null) {
					while (in.read() != -1) {
					}
				} else if (out != null) {
					out.flush();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (close) {
					quietClose(out);
				}
			}
		}

	}

	public static int i = 0;

	/**
	 * @param stdIn
	 * @param stdOut
	 * @param error
	 * @param command
	 * @param directory
	 * @throws Exception
	 */
	public static void execute(File directory, Reader stdIn, Writer stdOut, Writer error, String... command) throws Exception {

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(directory);

		final Process proc = pb.start();

		// 出力と入力を同時に行うとデッドロックになるので入力をスレッド化
		processIoWatch(stdIn, stdOut, error, proc);

	}

	public static void execute(File directory, String in, Writer stdOut, Writer error, String... command) throws Exception {

		StringReader stdIn = null;
		try {
			stdIn = new StringReader(in);

			execute(directory, stdIn, stdOut, error, command);
		} catch (Exception e) {
			throw e;
		} finally {
			quietClose(stdIn);
		}

	}

	private static void processIoThreadStart(Reader stdIn, Writer stdOut, Writer error, final Process proc, Writer out, Reader in, Reader err)
			throws InterruptedException {
		Thread stdInThread = new StreamPipeThread(stdIn, out, true);
		Thread stdOutThread = new StreamPipeThread(in, stdOut);
		Thread stdErrThread = new StreamPipeThread(err, error);

		stdInThread.start();
		stdOutThread.start();
		stdErrThread.start();

		proc.waitFor();

		stdOutThread.join();
		stdInThread.join();
		stdErrThread.join();
	}

	private static void processIoWatch(Reader stdIn, Writer stdOut, Writer error, final Process proc) throws Exception {
		Writer out = null;
		Reader in = null;
		Reader err = null;
		try {
			Charset charset = OSSupport.getOSConsoleCharset();
			out = new OutputStreamWriter(proc.getOutputStream(), charset);
			in = new InputStreamReader(proc.getInputStream(), charset);
			err = new InputStreamReader(proc.getErrorStream(), charset);
			processIoThreadStart(stdIn, stdOut, error, proc, out, in, err);
		} catch (Exception e) {
			throw e;
		} finally {
			quietClose(out);
			quietClose(in);
			quietClose(err);
		}
	}

	private static void quietClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
		}
	}

}
