package org.lo.d.eclipseplugin.mcp.commands;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole.NestedMessageConsoleStream;

public interface BuildCommand {
	public abstract class AbstractBuildCommand implements BuildCommand {
		private static class FilesCount {
			int files = 0;
			int dirs = 0;

			public FilesCount() {
			}

			public FilesCount(File file) {
				if (file.isDirectory()) {
					files = 1;
				} else {
					dirs = 1;
				}
			}

			private FilesCount add(FilesCount count) {
				files = files + count.files;
				dirs = dirs + count.dirs;
				return this;
			}
		}

		protected static final String[] EXCLUDE_FOLDERS = {
				"bin", "docs", "eclipse", "logs", "reobf", "src",
		};
		protected static final Pattern PATTERN_EXCLUDE_FOLDER;

		static {
			StringBuilder sb = new StringBuilder();
			String sep = "";
			for (String arg : EXCLUDE_FOLDERS) {
				sb.append(sep);
				sb.append(arg);
				sep = "|";
			}
			PATTERN_EXCLUDE_FOLDER = Pattern.compile(sb.toString());
		}

		private static final Pattern PATTERN_INCLUDE_FILE = Pattern.compile("(?:.*\\.bat)|(?:.*\\.sh)");

		protected static void quietClose(Closeable closeable) {
			if (closeable == null) {
				return;
			}
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}

		protected final MCPBuildProperty property;

		protected final NestedMessageConsoleStream out;

		protected final String name;

		protected IProgressMonitor monitor;

		protected AbstractBuildCommand(MCPBuildProperty property, NestMessageConsole console, String name) {
			this.property = property;
			out = console.newMessageStream();
			this.name = name;
		}

		@Override
		public int getCommandCount() {
			return 1;
		}

		@Override
		public void run(IProgressMonitor monitor) throws ExecutionException {
			this.monitor = monitor;
			monitor.beginTask(name, 100);
			out.nest();
			out.println("Start process: " + name);
			monitor.setTaskName(name);
			runCommand();
			out.println("End process: " + name);
			out.endNest();
		}

		@Deprecated
		protected void _creanDirectory(Path path) {
			File rootFile = path.toFile();
			if (!rootFile.isDirectory()) {
				return;
			}

			FilesCount fileCounts = new FilesCount();
			for (File file : rootFile.listFiles()) {
				fileCounts.add(deleteDirectoryAndFiles(file));
			}

			out.println(String.format("Cleaned %s. dirs:%d, files:%d.", path, fileCounts.dirs, fileCounts.files));
		}

		protected void copyFile(Path linkPath, Path entityPath, String ext) throws ExecutionException {
			try {
				if (!linkPath.toFile().exists()) {
					if (entityPath.toFile().isDirectory() || entityPath.toFile().getName().endsWith(ext)) {
						Files.copy(entityPath, linkPath);
					}
					// out.println(String.format("Copied %s from %s .",
					// linkPath, entityPath));
				}
				if (entityPath.toFile().isDirectory()) {
					for (File file : entityPath.toFile().listFiles()) {
						Path ePath = Paths.get(file.toURI());
						Path lPath = linkPath.resolve(ePath.getFileName());
						copyFile(lPath, ePath, ext);
					}
				}
			} catch (IOException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}

		protected void creanDirectory(Path path) {
			File rootFile = path.toFile();
			if (!rootFile.isDirectory()) {
				return;
			}

			File oldFile = getOldFile(path, 1);
			rootFile.renameTo(oldFile);

			rootFile.mkdirs();

			out.println(String.format("Cleaned %s. moved to %s.", path, oldFile.getName()));
		}

		protected void createDirectory(Path path) throws ExecutionException {
			if (path.toFile().exists()) {
				return;
			}
			try {
				Files.createDirectories(path);
				out.println(String.format("Directory %s created.", path));
			} catch (IOException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}

		protected void createLink(Path linkPath, Path entityPath) throws ExecutionException {
			if (linkPath.toFile().exists()) {
				return;
			}
			try {
				Files.createSymbolicLink(linkPath, entityPath);
				out.println(String.format("Linked %s --> %s created.", linkPath, entityPath));
			} catch (IOException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}

		protected FilesCount deleteDirectoryAndFiles(File file) {
			if (!file.exists()) {
				return new FilesCount();
			}
			FilesCount fileCounts = new FilesCount(file);
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					fileCounts.add(deleteDirectoryAndFiles(f));
				}
			}
			file.delete();
			return fileCounts;
		}

		protected void generateLinks(File mcpDir, Path rootPath) throws ExecutionException {
			File[] files = mcpDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) {
						Matcher matcher = PATTERN_EXCLUDE_FOLDER.matcher(pathname.getName());
						if (matcher.matches()) {
							return false;
						}
					} else {
						Matcher matcher = PATTERN_INCLUDE_FILE.matcher(pathname.getName());
						if (!matcher.matches()) {
							return false;
						}
					}
					return true;
				}
			});
			for (File file : files) {
				Path entityPath = Paths.get(file.toURI());
				Path linkPath = rootPath.resolve(entityPath.getFileName());
				createLink(linkPath, entityPath);
			}
		}

		protected abstract void runCommand() throws ExecutionException;

		private File getOldFile(Path path, int i) {
			File oldFile = path.resolveSibling(path.getFileName() + "_" + i).toFile();
			if (oldFile.exists()) {
				oldFile = getOldFile(path, ++i);
			}
			return oldFile;
		}
	}

	public static final String MCP_DIR_BIN = "bin/minecraft";

	public static final String MCP_DIR_SRC = "src/minecraft";

	public int getCommandCount();

	public void run(IProgressMonitor monitor) throws ExecutionException;
}
