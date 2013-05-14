package org.lo.d.eclipseplugin.mcp.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.lo.d.eclipseplugin.mcp.commands.BuildCommand.AbstractBuildCommand;
import org.lo.d.eclipseplugin.mcp.handlers.MCPBuildProperty;
import org.lo.d.eclipseplugin.mcp.handlers.NestMessageConsole;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemNode;

public class CompressCommand extends AbstractBuildCommand {

	public CompressCommand(MCPBuildProperty property, NestMessageConsole out) {
		super(property, out, "Compress");
	}

	@Override
	protected void runCommand() throws ExecutionException {
		final File root = property.getGenerateTempBuildLocation();
		Path rootPath = Paths.get(root.toURI());
		Path reobfPath = rootPath.resolve("reobf");
		compress(reobfPath);
	}

	private void compress(Path reobfPath) {
		File reobfDir = reobfPath.resolve("minecraft").toFile();
		ZipOutputStream zipOutputStream = null;
		try {
			if (!reobfDir.exists() || !reobfDir.isDirectory()) {
				return;
			}

			File zipLocation = property.getProject().getLocation().toFile();
			String zipFileName = property.getProperty().getOutputFileName();
			File zipFile = new File(zipLocation, zipFileName + ".zip");
			if (zipFile.exists()) {
				int i = 1;
				File oldFile = getOldFile(zipLocation, zipFileName, i);
				zipFile.renameTo(oldFile);
			}
			zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

			Set<DirectoryItemNode> resources = property.getProperty().getResourceLocations();
			if (resources == null) {
				resources = new HashSet<DirectoryItemNode>();
			}

			File[] listFiles = reobfDir.listFiles();
			SubProgressMonitor fMon;
			fMon = new SubProgressMonitor(monitor, 100);
			fMon.beginTask("Compress Zip to " + zipFile.getCanonicalPath(), listFiles.length + resources.size());
			fMon.setTaskName("Compress Zip to " + zipFile.getCanonicalPath());
			for (File file : listFiles) {
				fMon.subTask(file.getCanonicalPath());
				entryToZip(zipOutputStream, file, Paths.get(""));
				fMon.worked(1);
			}
			for (DirectoryItemNode node : resources) {
				File file = node.getFile();
				fMon.subTask(file.getCanonicalPath());
				entryToZip(zipOutputStream, file, Paths.get(""));
				fMon.worked(1);
			}
			fMon.done();

			zipOutputStream.finish();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			quietClose(zipOutputStream);
		}
	}

	private void entryToZip(ZipOutputStream zipOutputStream, File file, Path dirPath) throws IOException {
		Path entryPath = dirPath.resolve(file.getName());
		if (file.isDirectory()) {
			ZipEntry paramZipEntry = new ZipEntry(entryPath.toString() + "/");
			zipOutputStream.putNextEntry(paramZipEntry);
			for (File f : file.listFiles()) {
				entryToZip(zipOutputStream, f, entryPath);
			}
		} else {
			ZipEntry paramZipEntry = new ZipEntry(entryPath.toString());
			zipOutputStream.putNextEntry(paramZipEntry);
			BufferedInputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream(file));
				int b;
				while ((b = is.read()) != -1) {
					zipOutputStream.write(b);
				}
			} finally {
				quietClose(is);
			}
		}
	}

	private File getOldFile(File zipLocation, String zipFileName, int i) {
		File oldFile = new File(zipLocation, zipFileName + "_" + i + ".zip");
		if (oldFile.exists()) {
			oldFile = getOldFile(zipLocation, zipFileName, ++i);
		}
		return oldFile;
	}
}
