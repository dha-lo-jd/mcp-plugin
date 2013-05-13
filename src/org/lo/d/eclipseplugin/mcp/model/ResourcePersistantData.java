package org.lo.d.eclipseplugin.mcp.model;

import org.eclipse.core.resources.IResource;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter.ConversionException;

public interface ResourcePersistantData {
	public void load() throws ConversionException;

	public void loadDefault() throws ConversionException;

	public void save() throws ConversionException;

	public void setResource(IResource resource);
}