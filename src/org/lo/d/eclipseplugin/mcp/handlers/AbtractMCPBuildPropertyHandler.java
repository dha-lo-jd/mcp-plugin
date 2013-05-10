package org.lo.d.eclipseplugin.mcp.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IProject;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;

public abstract class AbtractMCPBuildPropertyHandler extends AbstractHandler implements MCPBuildProperty {

	protected File mcpLocation;
	protected File generateTempBuildLocation;
	protected IProject project;
	protected MCPPropertyModel.ValueAccessor property;

	public AbtractMCPBuildPropertyHandler() {
		super();
	}

	@Override
	public File getGenerateTempBuildLocation() {
		return generateTempBuildLocation;
	}

	@Override
	public File getMcpLocation() {
		return mcpLocation;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public MCPPropertyModel.ValueAccessor getProperty() {
		return property;
	}

}