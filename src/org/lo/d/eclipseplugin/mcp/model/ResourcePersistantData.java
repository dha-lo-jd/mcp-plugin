package org.lo.d.eclipseplugin.mcp.model;

import org.eclipse.core.resources.IResource;

public interface ResourcePersistantData {
	public void load();

	public void loadDefault();

	public void save();

	public void setResource(IResource resource);
}