package org.lo.d.eclipseplugin.mcp.handlers;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;

public interface MCPBuildProperty {

	public File getMcpLocation();

	public File getGenerateTempBuildLocation();

	public IProject getProject();

	public MCPPropertyModel.ValueAccessor getProperty();

}