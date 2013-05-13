package org.lo.d.eclipseplugin.mcp.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter.ConversionException;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.PerseListener;

public abstract class ResourcePersistantPropertyListenerModel extends AbstractPropertyListenerModel implements ResourcePersistantData {

	public interface ListenerCollectionResourcePersistantProperty<V> extends ListenerCollectionProperty<V>, ListenerResourcePersistantProperty<V> {
	}

	public interface ListenerResourcePersistantProperty<V> extends ListenerProperty<V>, ResourcePersistantData {
	}

	protected static class ListenerCollectionResourcePersistantPropertyImpl<V, CL extends Collection<V>> extends
			ListenerCollectionPropertyImpl<V, StringSerializerCollection<V, CL>> implements ListenerCollectionResourcePersistantProperty<V> {

		private final PerseListener<V> persistantValuePerseListener = new PerseListener<V>() {
			@Override
			public void handle(V value) {
				ListenerCollectionResourcePersistantPropertyImpl.this.add(value);
			}
		};

		private IResource resource;
		private final QualifiedName key;

		private final String defaultValue;

		protected ListenerCollectionResourcePersistantPropertyImpl(String qualifier, String localName, String defaultValue, CL values, Converter<V> converter) {
			super(new StringSerializerCollection<V, CL>(values, converter));
			this.key = new QualifiedName(qualifier, localName);
			this.defaultValue = defaultValue;
		}

		@Override
		public void load() throws ConversionException {
			try {
				String value = resource.getPersistentProperty(key);
				if (value != null) {
					values.parse(value, persistantValuePerseListener);
				} else {
					loadDefault();
				}
			} catch (CoreException e) {
				loadDefault();
			} catch (ConversionException e) {
				loadDefault();
			}
		}

		@Override
		public void loadDefault() throws ConversionException {
			clear();
			values.parse(defaultValue, persistantValuePerseListener);
		}

		@Override
		public void save() throws ConversionException {
			try {
				resource.setPersistentProperty(key, values.serialize());
			} catch (CoreException e) {
			}
		}

		@Override
		public void setResource(IResource resource) {
			this.resource = resource;
		}

	}

	protected static class ListenerResourcePersistantPropertyImpl<V> extends ListenerPropertyImpl<V> implements ListenerResourcePersistantProperty<V> {

		public interface Converter<V> {
			String toString(V value);

			V valueOf(String str);
		}

		private IResource resource;

		private final QualifiedName key;

		private final V defaultValue;
		private final Converter<V> converter;

		protected ListenerResourcePersistantPropertyImpl(String qualifier, String localName, V defaultValue, Converter<V> converter) {
			super();
			this.key = new QualifiedName(qualifier, localName);
			this.defaultValue = defaultValue;
			this.converter = converter;
		}

		@Override
		public void load() {
			try {
				String value = resource.getPersistentProperty(key);
				if (value != null) {
					add(converter.valueOf(value));
				} else {
					loadDefault();
				}
			} catch (CoreException e) {
				loadDefault();
			}
		}

		@Override
		public void loadDefault() {
			add(defaultValue);
		}

		@Override
		public void save() {
			try {
				resource.setPersistentProperty(key, converter.toString(getValue()));
			} catch (CoreException e) {
			}
		}

		@Override
		public void setResource(IResource resource) {
			this.resource = resource;
		}
	}

	private final Set<Field> fields;

	public ResourcePersistantPropertyListenerModel() {
		fields = getField(ListenerResourcePersistantProperty.class, this.getClass());
	}

	@Override
	public void load() throws ConversionException {
		for (ListenerResourcePersistantProperty property : getProperties()) {
			property.load();
		}
	}

	@Override
	public void loadDefault() throws ConversionException {
		for (ListenerResourcePersistantProperty property : getProperties()) {
			property.loadDefault();
		}
	}

	@Override
	public void save() throws ConversionException {
		for (ListenerResourcePersistantProperty property : getProperties()) {
			property.save();
		}
	}

	@Override
	public void setResource(IResource resource) {
		for (ListenerResourcePersistantProperty property : getProperties()) {
			property.setResource(resource);
		}
	}

	private Iterable<ListenerResourcePersistantProperty> getProperties() {
		return getProperties(this, fields);
	}
}
