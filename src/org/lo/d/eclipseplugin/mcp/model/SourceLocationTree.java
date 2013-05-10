package org.lo.d.eclipseplugin.mcp.model;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter;

public class SourceLocationTree extends ResourcePersistantPropertyListenerModel {

	public static abstract class AbstractNode implements Node {
		@Override
		public int compareTo(Node o) {
			return getName().compareTo(o.getName());
		}

		@Override
		public Node[] getChildren() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		public String getSerializeKey() {
			return getName();
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public String serializedString() {
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

	public static abstract class AbstractSubTreeNode<SUB extends Node> extends
			AbstractNode {
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
		public String serializedString();

		Node[] getChildren();

		String getName();

		Node getParent();

		boolean hasChildren();
	}

	public static class ProjectNode extends AbstractSubTreeNode<SourceNode> {
		private final WorkspaceNode parent;
		private final IJavaProject project;

		private ProjectNode(WorkspaceNode parent, IJavaProject project) {
			this.parent = parent;
			this.project = project;
			try {
				for (IPackageFragmentRoot fragmentRoot : project
						.getPackageFragmentRoots()) {
					if (fragmentRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
						continue;
					}
					if (fragmentRoot.getResource() != null) {
						childs.add(new SourceNode(this, fragmentRoot));
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getName() {
			return project.getElementName();
		}

		@Override
		public Node getParent() {
			return parent;
		}

		@Override
		protected SourceNode[] createChildrenArray() {
			return new SourceNode[] {};
		}
	}

	public static class SourceNode extends AbstractNode {

		private final ProjectNode parent;
		private final IPackageFragmentRoot fragmentRoot;

		private SourceNode(ProjectNode parent, IPackageFragmentRoot fragmentRoot) {
			super();
			this.parent = parent;
			this.fragmentRoot = fragmentRoot;
		}

		@Override
		public String getName() {
			return fragmentRoot.getElementName() + " : " + getPath();
		}

		@Override
		public Node getParent() {
			return parent;
		}

		public String getPath() {
			return fragmentRoot.getResource().getLocation().toString();
		}

		@Override
		public String getSerializeKey() {
			return fragmentRoot.getElementName();
		}
	}

	public static class SourceNodeConverter implements Converter<SourceNode> {
		private final Node root;

		public SourceNodeConverter(Node root) {
			this.root = root;
		}

		@Override
		public String toString(SourceNode value) {
			return value.serializedString();
		}

		@Override
		public SourceNode valueOf(String str) throws ConversionException {
			Node node = searchNode(str, root);
			if (node == null || !(node instanceof SourceNode)) {
				throw new ConversionException();
			}
			return (SourceNode) node;
		}

		private Node searchNode(String str, Node node) {
			if (str.equals(node.serializedString())) {
				return node;
			}

			Node[] children = node.getChildren();
			if (children == null) {
				return null;
			}

			for (Node child : children) {
				Node n = searchNode(str, child);
				if (n != null) {
					return n;
				}
			}
			return null;
		}
	}

	public static class WorkspaceNode extends AbstractSubTreeNode<ProjectNode> {
		private final IWorkspaceRoot workspace;

		public WorkspaceNode(IWorkspaceRoot workspace) {
			this.workspace = workspace;
			for (IProject project : workspace.getProjects()) {
				if (!project.exists() || !project.isAccessible()
						|| project.isPhantom() || project.isHidden()) {
					continue;
				}
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null) {
					try {
						javaProject.getPackageFragmentRoots();
						childs.add(new ProjectNode(this, javaProject));
					} catch (Exception e) {
					}
				}
			}
		}

		@Override
		public String getName() {
			return workspace.getName();
		}

		@Override
		public Node getParent() {
			return null;
		}

		@Override
		protected ProjectNode[] createChildrenArray() {
			return new ProjectNode[] {};
		}
	}

}
