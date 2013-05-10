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
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;

public interface BuildCommand {
	public abstract class AbstractBuildCommand implements BuildCommand {
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
		protected final NestMessageConsole out;

		protected final String name;

		protected AbstractBuildCommand(MCPBuildProperty property, NestMessageConsole out, String name) {
			this.property = property;
			this.out = out;
			this.name = name;
		}

		@Override
		public void run() throws ExecutionException {
			out.nest();
			out.println("Start process: " + name);
			runCommand();
			out.println("End process: " + name);
			out.endNest();
		}

		protected void copyFile(Path linkPath, Path entityPath) throws ExecutionException {
			try {
				if (!linkPath.toFile().exists()) {
					Files.copy(entityPath, linkPath);
					// out.println(String.format("Copied %s from %s .",
					// linkPath, entityPath));
				}
				if (entityPath.toFile().isDirectory()) {
					for (File file : entityPath.toFile().listFiles()) {
						Path ePath = Paths.get(file.toURI());
						Path lPath = linkPath.resolve(ePath.getFileName());
						copyFile(lPath, ePath);
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

			for (File file : rootFile.listFiles()) {
				deleteDirectoryAndFiles(file);
			}
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

		protected void deleteDirectoryAndFiles(File file) {
			if (!file.exists()) {
				return;
			}
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					deleteDirectoryAndFiles(f);
				}
			}
			file.delete();
		}

		protected void generateLinks(File mcpDir, Path rootPath) throws ExecutionException {
			File[] files = mcpDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					System.out.println(pathname.getAbsolutePath());
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
			System.out.println();
			for (File file : files) {
				System.out.println(file.getAbsolutePath());

				Path entityPath = Paths.get(file.toURI());
				Path linkPath = rootPath.resolve(entityPath.getFileName());
				createLink(linkPath, entityPath);
			}
		}

		protected abstract void runCommand() throws ExecutionException;
	}

	public void run() throws ExecutionException;
}
