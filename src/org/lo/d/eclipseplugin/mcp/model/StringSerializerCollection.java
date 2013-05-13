package org.lo.d.eclipseplugin.mcp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter.ConversionException;

public class StringSerializerCollection<E, CL extends Collection<E>> implements Collection<E> {
	public interface Converter<E> {
		public class ConversionException extends Exception {

			public ConversionException() {
				super();
			}

			public ConversionException(String arg0) {
				super(arg0);
			}

			public ConversionException(String message, Throwable cause) {
				super(message, cause);
			}

			public ConversionException(Throwable cause) {
				super(cause.getMessage(), cause);
			}
		}

		String toString(E value) throws ConversionException;

		E valueOf(String str) throws ConversionException;
	}

	public interface PerseListener<E> {
		public void handle(E value);
	}

	private static final String DELIMITER = ";";

	public static <E> StringSerializerCollection<E, List<E>> newInstance(Converter<E> converter) {
		return newInstance(DELIMITER, converter);
	}

	public static <E> StringSerializerCollection<E, List<E>> newInstance(String delimiter, Converter<E> converter) {
		return new StringSerializerCollection<E, List<E>>(new ArrayList<E>(), delimiter, converter);
	}

	public static <E> StringSerializerCollection<E, List<E>> newParsedInstance(String str, Converter<E> converter) throws ConversionException {
		return newParsedInstance(str, DELIMITER, converter);
	}

	public static <E> StringSerializerCollection<E, List<E>> newParsedInstance(String str, String delimiter, Converter<E> converter) throws ConversionException {
		return newInstance(delimiter, converter).parse(str);
	}

	private final CL collection;
	private final String delimiter;

	private final Converter<E> converter;

	private final PerseListener<E> simplePerseListener = new PerseListener<E>() {
		@Override
		public void handle(E value) {
			collection.add(value);
		}
	};

	public StringSerializerCollection(CL collection, Converter<E> converter) {
		this(collection, DELIMITER, converter);
	}

	private StringSerializerCollection(CL collection, String delimiter, Converter<E> converter) {
		super();
		this.collection = collection;
		this.delimiter = delimiter;
		this.converter = converter;
	}

	@Override
	public boolean add(E e) {
		return collection.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return collection.addAll(c);
	}

	@Override
	public void clear() {
		collection.clear();
	}

	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return collection.iterator();
	}

	public StringSerializerCollection<E, CL> parse(String str) throws ConversionException {
		return parse(str, simplePerseListener);
	}

	public StringSerializerCollection<E, CL> parse(String str, PerseListener<E> listener) {
		if (str == null || str.isEmpty()) {
			return this;
		}
		String[] args = str.split(delimiter);
		if (args == null || args.length == 0) {
			return this;
		}

		for (String arg : args) {
			try {
				listener.handle(converter.valueOf(arg));
			} catch (ConversionException e) {
			}
		}

		return this;
	}

	@Override
	public boolean remove(Object o) {
		return collection.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return collection.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return collection.retainAll(c);
	}

	public String serialize() throws ConversionException {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (E element : collection) {
			sb.append(sep);
			sb.append(converter.toString(element));
			sep = delimiter;
		}
		return sb.toString();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public Object[] toArray() {
		return collection.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return collection.toArray(a);
	}
}
