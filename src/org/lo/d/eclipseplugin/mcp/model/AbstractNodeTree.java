package org.lo.d.eclipseplugin.mcp.model;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.lo.d.eclipseplugin.mcp.model.AbstractNodeTree.AbstractNode.SerializeFailedException;

public abstract class AbstractNodeTree extends ResourcePersistantPropertyListenerModel {

	public static abstract class AbstractModifiableSubTreeNode<N extends AbstractModifiableSubTreeNode, SUB extends Node, E> extends AbstractSubTreeNode<SUB> {
		public interface SubTreeNodeFactory<N extends AbstractModifiableSubTreeNode, SUB extends Node, E> {
			public SUB create(E element);

			public void setParent(N node);
		}

		private final SubTreeNodeFactory<N, SUB, E> factory;

		protected AbstractModifiableSubTreeNode(SubTreeNodeFactory<N, SUB, E> factory) {
			this.factory = factory;
			factory.setParent(getSelf());
		}

		public SUB add(E element) {
			SUB sub = factory.create(element);
			add(sub);
			return sub;
		}

		public void add(SUB element) {
			childs.add(element);
		}

		public SUB remove(E element) {
			SUB sub = factory.create(element);
			remove(sub);
			return sub;
		}

		public void remove(SUB element) {
			childs.remove(element);
		}

		protected abstract N getSelf();
	}

	public static abstract class AbstractNode implements Node {
		public class SerializeFailedException extends Exception {

			public SerializeFailedException() {
				super();
			}

			public SerializeFailedException(String message, Throwable cause) {
				super(message, cause);
			}

			public SerializeFailedException(Throwable cause) {
				super(cause.getMessage(), cause);
			}
		}

		@Override
		public void buildLabel(StyledString styledString) {
			styledString.append(getName());
		}

		@Override
		public int compareTo(Node o) {
			return getName().compareTo(o.getName());
		}

		@Override
		public Node[] getChildren() {
			return null;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getName() {
			return toString();
		}

		public String getSerializeKey() throws SerializeFailedException {
			return getName();
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public String serializedString() throws SerializeFailedException {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if (getParent() != null) {
				sb.append("p=");
				sb.append(getParent().serializedString());
				sb.append(", ");
			}
			sb.append(getSerializeKey());
			sb.append("]");
			return sb.toString();
		}
	}

	public static abstract class AbstractSubTreeNode<SUB extends Node> extends AbstractNode {
		protected final Set<SUB> childs = new TreeSet<SUB>();

		@Override
		public SUB[] getChildren() {
			return childs.toArray(createChildrenArray());
		}

		@Override
		public boolean hasChildren() {
			Object[] children = getChildren();
			return children != null && children.length > 0;
		}

		protected abstract SUB[] createChildrenArray();
	}

	public interface Node extends Comparable<Node> {
		public void buildLabel(StyledString styledString);

		public Image getImage();

		public String serializedString() throws SerializeFailedException;

		Node[] getChildren();

		String getName();

		Node getParent();

		boolean hasChildren();
	}

	public AbstractNodeTree() {
		super();
	}

}