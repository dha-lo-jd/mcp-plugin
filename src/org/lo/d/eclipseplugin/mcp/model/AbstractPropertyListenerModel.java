package org.lo.d.eclipseplugin.mcp.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPropertyListenerModel {

	public interface ListenerCollectionProperty<T> extends ListenerProperty<T>,
			Iterable<T> {
		public void clear();
	}

	public interface ListenerProperty<T> extends ValueReceiver<T> {
		public boolean addReceiver(ValueReceiver<T> receiver);

		public ValueReceiver<T> getReceiver();
	}

	public interface ValueReceiver<T> {
		public void add(T value);

		public void remove(T value);
	}

	protected static abstract class AbstractListenerProperty<T> implements
			ListenerProperty<T> {

		public class Receiver implements ValueReceiver<T> {
			@Override
			public void add(T value) {
				AbstractListenerProperty.this.updateValue(value);
			}

			@Override
			public void remove(T value) {
				AbstractListenerProperty.this.removeValue(value);
			}
		}

		private Set<ValueReceiver<T>> listener = new LinkedHashSet<AbstractPropertyListenerModel.ValueReceiver<T>>();

		private final Receiver receiver = new Receiver();

		@Override
		public void add(T value) {
			receiver.add(value);
		}

		@Override
		public boolean addReceiver(ValueReceiver<T> receiver) {
			initializeReceiver(receiver);
			return listener.add(receiver);
		}

		@Override
		public Receiver getReceiver() {
			return receiver;
		}

		@Override
		public void remove(T value) {
			receiver.remove(value);
		}

		abstract protected void doRemoveValue(T value);

		abstract protected void doUpdateValue(T value);

		abstract protected void initializeReceiver(ValueReceiver<T> receiver);

		private void removeValue(T value) {
			doRemoveValue(value);
			for (ValueReceiver<T> receiver : listener) {
				receiver.remove(value);
			}
		}

		private void updateValue(T value) {
			doUpdateValue(value);
			for (ValueReceiver<T> receiver : listener) {
				receiver.add(value);
			}
		}
	}

	protected static class ListenerCollectionPropertyImpl<T, CL extends Collection<T>>
			extends AbstractListenerProperty<T> implements
			ListenerCollectionProperty<T> {
		protected final CL values;

		protected ListenerCollectionPropertyImpl(CL values) {
			this.values = values;
		}

		@Override
		public void clear() {
			List<T> oldValues = new ArrayList<T>(values);
			for (T value : oldValues) {
				remove(value);
			}
		}

		@Override
		public Iterator<T> iterator() {
			return values.iterator();
		}

		@Override
		protected void doRemoveValue(T value) {
			values.remove(value);
		}

		@Override
		protected void doUpdateValue(T value) {
			values.add(value);
		}

		@Override
		protected void initializeReceiver(ValueReceiver<T> receiver) {
			for (T value : values) {
				receiver.add(value);
			}
		}

	}

	protected static class ListenerPropertyImpl<T> extends
			AbstractListenerProperty<T> {
		private T value;

		protected ListenerPropertyImpl() {
		}

		@Override
		protected void doRemoveValue(T value) {
		}

		@Override
		protected void doUpdateValue(T value) {
			this.value = value;
		}

		protected T getValue() {
			return value;
		}

		@Override
		protected void initializeReceiver(ValueReceiver<T> receiver) {
			receiver.add(value);
		}

	}

	protected <T extends ListenerProperty> Set<Field> getField(Class<T> type,
			Class<?> modelClass) {
		Set<Field> fields = new HashSet<Field>();
		Class<?> superClass = modelClass.getSuperclass();
		if (superClass != null
				&& AbstractPropertyListenerModel.class
						.isAssignableFrom(superClass)) {
			fields.addAll(getField(type, superClass));
		}

		for (Field field : modelClass.getDeclaredFields()) {
			if (type.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				fields.add(field);
			}
		}

		return fields;
	}

	protected <T extends ListenerProperty> Iterable<T> getProperties(
			AbstractPropertyListenerModel model, Set<Field> fields) {
		Set<T> result = new HashSet<T>();
		for (Field f : fields) {
			try {
				result.add((T) f.get(model));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
			}
		}
		return result;
	}
}
