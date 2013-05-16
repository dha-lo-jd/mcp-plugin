package org.lo.d.eclipseplugin.mcp.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.lo.d.eclipseplugin.mcp.model.AbstractNodeTree.AbstractNode.SerializeFailedException;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter;

public class SourceLocationTree extends AbstractNodeTree {

	public static class FragmentRootSourceNode extends SourceNode {
		private FragmentRootSourceNode(ProjectNode parent, IPackageFragmentRoot fragmentRoot) {
			super(parent, fragmentRoot.getResource().getLocation(), fragmentRoot.getElementName());
		}
	}

	public static class ProjectNode extends AbstractSubTreeNode<SourceNode> {
		private final WorkspaceNode parent;
		private final IJavaProject project;

		private ProjectNode(WorkspaceNode parent, IJavaProject project, Type type) {
			this.parent = parent;
			this.project = project;
			try {
				switch (type) {
				case BINARY:
					Set<IPath> outputLocations = new HashSet<IPath>();
					outputLocations.add(project.getOutputLocation());
					for (IClasspathEntry classpathEntry : project.getRawClasspath()) {
						if (classpathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
							continue;
						}
						IPath outputLocation = classpathEntry.getOutputLocation();
						if (outputLocation != null) {
							outputLocations.add(outputLocation);
						}
					}
					for (IPath path : outputLocations) {
						childs.add(new SourceOutputNode(this, path, project.getProject()));
					}
					break;
				case SOURCE:
					for (IPackageFragmentRoot fragmentRoot : project.getAllPackageFragmentRoots()) {
						if (fragmentRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
							continue;
						}
						if (fragmentRoot.getResource() != null) {
							childs.add(new FragmentRootSourceNode(this, fragmentRoot));
						}
					}
					break;
				default:
					break;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		@Override
		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
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

	public abstract static class SourceNode extends AbstractNode {

		protected final ProjectNode parent;
		protected final IPath path;
		protected final String name;

		protected SourceNode(ProjectNode parent, IPath path, String name) {
			super();
			this.parent = parent;
			this.path = path;
			this.name = name;
		}

		@Override
		public void buildLabel(StyledString styledString) {
			styledString.append(name);
			styledString.append(" : ", StyledString.DECORATIONS_STYLER);
			styledString.append(getPath(), StyledString.DECORATIONS_STYLER);
		}

		@Override
		public Image getImage() {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
		}

		@Override
		public Node getParent() {
			return parent;
		}

		public String getPath() {
			return path.toString();
		}

		@Override
		public String getSerializeKey() {
			return name;
		}
	}

	public static class SourceNodeConverter implements Converter<SourceNode> {
		private final Node root;

		public SourceNodeConverter(Node root) {
			this.root = root;
		}

		@Override
		public String toString(SourceNode value) {
			try {
				return value.serializedString();
			} catch (SerializeFailedException e) {
				return null;
			}
		}

		@Override
		public SourceNode valueOf(String str) throws ConversionException {
			Node node = searchNode(str, root);
			if (node == null || !(node instanceof SourceNode)) {
				throw new ConversionException(str);
			}
			return (SourceNode) node;
		}

		private Node searchNode(String str, Node node) {
			try {
				if (str.equals(node.serializedString())) {
					return node;
				}
			} catch (SerializeFailedException e) {
				return null;
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

	public static class SourceOutputNode extends SourceNode {
		protected SourceOutputNode(ProjectNode parent, IPath path, IProject project) {
			super(parent, project.getFolder(path.removeFirstSegments(1)).getLocation(), path.toString());
		}
	}

	public enum Type {
		SOURCE, BINARY, ;
	}

	public static class WorkspaceBinaryNode extends WorkspaceNode {
		public WorkspaceBinaryNode(IWorkspaceRoot workspace) {
			super(workspace, Type.BINARY);
		}

	}

	public static class WorkspaceNode extends AbstractSubTreeNode<ProjectNode> {
		private final IWorkspaceRoot workspace;

		public WorkspaceNode(IWorkspaceRoot workspace, Type type) {
			this.workspace = workspace;
			for (IProject project : workspace.getProjects()) {
				if (!project.exists() || !project.isAccessible() || project.isPhantom() || project.isHidden()) {
					continue;
				}
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					try {
						javaProject.getPackageFragmentRoots();
						childs.add(new ProjectNode(this, javaProject, type));
					} catch (Exception e) {
						e.printStackTrace();
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

	public static class WorkspaceSourceNode extends WorkspaceNode {
		public WorkspaceSourceNode(IWorkspaceRoot workspace) {
			super(workspace, Type.SOURCE);
		}

	}

}
