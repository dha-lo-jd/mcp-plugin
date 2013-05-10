package org.lo.d.eclipseplugin.mcp.resource.support;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectPathResolver {

	private static final Pattern PATTERN_PATH_SEPARATOR = Pattern.compile("[\\\\/]");

	public static File getResolvedFile(String pathString, IProject project) throws JavaModelException, URISyntaxException {
		File file = null;
		try {
			File f = new File(pathString);
			if (f != null && f.isAbsolute()) {
				file = new File(f.toURI().normalize());
			}
		} catch (Exception e) {
		}
		if (file != null && file.isAbsolute()) {
			return file;
		} else {
			return resolveFilePathURI(pathString, project);
		}
	}

	private static File resolveFilePathURI(String pathString, IProject project) throws URISyntaxException, JavaModelException {
		URI rootPath = new URI(project.getLocationURI() + "/").normalize();
		URI path = resolveIncludeReferenseProject(project, pathString);
		System.out.println("path: " + path);
		URI relativizeURI = rootPath.relativize(path);
		System.out.println("relativizeURI: " + relativizeURI);
		URI resolveURI;
		if (!relativizeURI.isAbsolute()) {
			resolveURI = rootPath.resolve(relativizeURI);
		} else {
			resolveURI = relativizeURI;
		}
		System.out.println("resolveURI: " + resolveURI);
		File file = new File(resolveURI);
		if (file != null && file.isAbsolute()) {
			return file;
		} else {
			return null;
		}
	}

	private static URI resolveIncludeReferenseProject(IProject project, String pathString) throws URISyntaxException, JavaModelException {
		Matcher matcher = PATTERN_PATH_SEPARATOR.matcher(pathString);
		URI prePath = new URI(matcher.replaceAll("/"));
		URI path = project.getProject().getPathVariableManager().resolveURI(prePath);
		if (path.equals(prePath)) {
			for (IProject referencingProject : project.getProject().getReferencingProjects()) {
				path = referencingProject.getPathVariableManager().resolveURI(prePath);
				if (!path.equals(prePath)) {
					break;
				}
			}
			if (path.equals(prePath)) {
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null) {
					for (String projectName : javaProject.getRequiredProjectNames()) {
						IProject referencingProject = javaProject.getProject().getWorkspace().getRoot().getProject(projectName);
						path = referencingProject.getProject().getPathVariableManager().resolveURI(prePath);
						if (!path.equals(prePath)) {
							break;
						}
					}
				}
			}
		}
		return path;
	}
}
