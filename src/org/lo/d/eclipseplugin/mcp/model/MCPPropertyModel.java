package org.lo.d.eclipseplugin.mcp.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.lo.d.eclipseplugin.mcp.model.AbstractNodeTree.Node;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemNode;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemNodeConverter;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.DirectoryItemRootNode;
import org.lo.d.eclipseplugin.mcp.model.ResourcePersistantPropertyListenerModel.ListenerResourcePersistantPropertyImpl.Converter;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNodeConverter;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.WorkspaceNode;

public class MCPPropertyModel extends ResourcePersistantPropertyListenerModel {
	public static class ValueAccessor {
		private static abstract class SimpleValueReceiver<T> implements ValueReceiver<T> {
			@Override
			public void remove(T value) {
			}
		}

		private String mcpLocation;
		private String generateTempBuildLocation;

		private String outputFileName;

		private Set<SourceNode> dependencySrcLocations = new HashSet<SourceNode>();

		private Set<SourceNode> targetSrcLocations = new HashSet<SourceNode>();

		private Set<DirectoryItemNode> resourceLocations = new HashSet<DirectoryItemNode>();

		private ValueAccessor(MCPPropertyModel propertyModel) {
			propertyModel.getMcpLocation().addReceiver(new SimpleValueReceiver<String>() {
				@Override
				public void add(String value) {
					mcpLocation = value;
				}
			});
			propertyModel.getGenerateTempBuildLocation().addReceiver(new SimpleValueReceiver<String>() {
				@Override
				public void add(String value) {
					generateTempBuildLocation = value;
				}
			});
			propertyModel.getOutputFileName().addReceiver(new SimpleValueReceiver<String>() {
				@Override
				public void add(String value) {
					outputFileName = value;
				}
			});
			propertyModel.getDependencySrcLocations().addReceiver(new SimpleValueReceiver<SourceNode>() {
				@Override
				public void add(SourceNode value) {
					dependencySrcLocations.add(value);
				}

				@Override
				public void remove(SourceNode value) {
					dependencySrcLocations.remove(value);
				}
			});
			propertyModel.getTargetSrcLocations().addReceiver(new SimpleValueReceiver<SourceNode>() {
				@Override
				public void add(SourceNode value) {
					targetSrcLocations.add(value);
				}

				@Override
				public void remove(SourceNode value) {
					targetSrcLocations.remove(value);
				}
			});
			propertyModel.getResourceLocations().addReceiver(new SimpleValueReceiver<DirectoryItemNode>() {
				@Override
				public void add(DirectoryItemNode value) {
					resourceLocations.add(value);
				}

				@Override
				public void remove(DirectoryItemNode value) {
					resourceLocations.remove(value);
				}
			});
		}

		public Set<SourceNode> getDependencySrcLocations() {
			return new HashSet(dependencySrcLocations);
		}

		public String getGenerateTempBuildLocation() {
			return generateTempBuildLocation;
		}

		public String getMcpLocation() {
			return mcpLocation;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public Set<DirectoryItemNode> getResourceLocations() {
			return resourceLocations;
		}

		public Set<SourceNode> getTargetSrcLocations() {
			return new HashSet(targetSrcLocations);
		}
	}

	private final static class LocationConverter implements Converter<String> {
		@Override
		public String toString(String value) {
			return value;
		}

		@Override
		public String valueOf(String str) {
			return str;
		}
	}

	private static final String DEFAULT_MCP_BUILD_TEMP = "./.mcp_build_Temp";

	private static final String DEFAULT_MCP_LOC = "MCP_LOC";
	private static final String DEFAULT_EXCLUDE_MCP_FOLDER = "MCP_LOC";

	private final ListenerCollectionResourcePersistantProperty<SourceNode> dependencySrcLocations;
	private final ListenerCollectionResourcePersistantProperty<SourceNode> targetSrcLocations;

	private final ListenerResourcePersistantProperty<String> mcpLocation;
	private final ListenerResourcePersistantProperty<String> generateTempBuildLocation;

	private final ListenerCollectionResourcePersistantProperty<DirectoryItemNode> resourceLocations;

	private final ListenerResourcePersistantProperty<String> outputFileName;

	private final ValueAccessor valueAccessor;

	private final DirectoryItemRootNode resourceLocationRootNode;

	public MCPPropertyModel(String key, WorkspaceNode root, DirectoryItemRootNode directoryItemRootNode, IProject project) {
		dependencySrcLocations = new ListenerCollectionResourcePersistantPropertyImpl(key, "dependencySrcLocations", "", new HashSet<Node>(),
				new SourceNodeConverter(root));
		targetSrcLocations = new ListenerCollectionResourcePersistantPropertyImpl(key, "targetSrcLocations", "", new HashSet<Node>(), new SourceNodeConverter(
				root));
		mcpLocation = new ListenerResourcePersistantPropertyImpl(key, "mcpLocation", DEFAULT_MCP_LOC, new LocationConverter());
		generateTempBuildLocation = new ListenerResourcePersistantPropertyImpl(key, "generateTempBuildLocation", DEFAULT_MCP_BUILD_TEMP,
				new LocationConverter());

		resourceLocationRootNode = directoryItemRootNode;
		resourceLocations = new ListenerCollectionResourcePersistantPropertyImpl(key, "resourceLocations", "", new HashSet<Node>(),
				new DirectoryItemNodeConverter(directoryItemRootNode));

		outputFileName = new ListenerResourcePersistantPropertyImpl(key, "outputFileName", "mod_" + project.getName(), new LocationConverter());

		valueAccessor = new ValueAccessor(this);
	}

	public ListenerCollectionResourcePersistantProperty<SourceNode> getDependencySrcLocations() {
		return dependencySrcLocations;
	}

	public ListenerResourcePersistantProperty<String> getGenerateTempBuildLocation() {
		return generateTempBuildLocation;
	}

	public ListenerResourcePersistantProperty<String> getMcpLocation() {
		return mcpLocation;
	}

	public ListenerResourcePersistantProperty<String> getOutputFileName() {
		return outputFileName;
	}

	public ListenerCollectionResourcePersistantProperty<DirectoryItemNode> getResourceLocations() {
		return resourceLocations;
	}

	public ListenerCollectionResourcePersistantProperty<SourceNode> getTargetSrcLocations() {
		return targetSrcLocations;
	}

	public ValueAccessor getValueAccessor() {
		return valueAccessor;
	}
}
