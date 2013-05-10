package org.lo.d.eclipseplugin.mcp.model;

import java.util.HashSet;
import java.util.Set;

import org.lo.d.eclipseplugin.mcp.model.ResourcePersistantPropertyListenerModel.ListenerResourcePersistantPropertyImpl.Converter;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.Node;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNode;
import org.lo.d.eclipseplugin.mcp.model.SourceLocationTree.SourceNodeConverter;

public class MCPPropertyModel extends ResourcePersistantPropertyListenerModel {
	public static class ValueAccessor {
		private static abstract class SimpleValueReceiver<T> implements ValueReceiver<T> {
			@Override
			public void remove(T value) {
			}
		}

		private String mcpLocation;
		private String generateTempBuildLocation;

		private Set<SourceNode> dependencySrcLocations = new HashSet<SourceNode>();

		private Set<SourceNode> targetSrcLocations = new HashSet<SourceNode>();

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

	private final ValueAccessor valueAccessor;

	public MCPPropertyModel(String key, Node root) {
		dependencySrcLocations = new ListenerCollectionResourcePersistantPropertyImpl(key, "dependencySrcLocations", "", new HashSet<Node>(),
				new SourceNodeConverter(root));
		targetSrcLocations = new ListenerCollectionResourcePersistantPropertyImpl(key, "targetSrcLocations", "", new HashSet<Node>(), new SourceNodeConverter(
				root));
		mcpLocation = new ListenerResourcePersistantPropertyImpl(key, "mcpLocation", DEFAULT_MCP_LOC, new LocationConverter());
		generateTempBuildLocation = new ListenerResourcePersistantPropertyImpl(key, "generateTempBuildLocation", DEFAULT_MCP_BUILD_TEMP,
				new LocationConverter());

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

	public ListenerCollectionResourcePersistantProperty<SourceNode> getTargetSrcLocations() {
		return targetSrcLocations;
	}

	public ValueAccessor getValueAccessor() {
		return valueAccessor;
	}
}
