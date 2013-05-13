package org.lo.d.eclipseplugin.mcp.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.lo.d.eclipseplugin.mcp.model.DirectoryItems.FileNode.ChildNodeFactory;
import org.lo.d.eclipseplugin.mcp.model.StringSerializerCollection.Converter;

public class DirectoryItems extends AbstractNodeTree {

	public static abstract class AbstractViewFileNode extends FileNode<FileNode, ViewFileNode> {
		protected AbstractViewFileNode(FileNode parent, File file) {
			super(parent, file, new Factory());
		}

	}

	public static class DirectoryItemNode extends FileNode<DirectoryItemRootNode, ViewFileNode> {
		public DirectoryItemNode(DirectoryItemRootNode parent, File file) {
			super(parent, file, new Factory());
		}

		@Override
		public void buildLabel(StyledString styledString) {
			try {
				String canonicalPath = file.getCanonicalPath();
				styledString.append(file.getName());
				styledString.append(" : ", StyledString.DECORATIONS_STYLER);
				styledString.append(canonicalPath, StyledString.DECORATIONS_STYLER);
			} catch (IOException e) {
				super.buildLabel(styledString);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DirectoryItemNode other = (DirectoryItemNode) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}

		@Override
		protected ViewFileNode[] createChildrenArray() {
			return new ViewFileNode[] {};
		}
	};

	public static class DirectoryItemNodeConverter implements Converter<DirectoryItemNode> {
		private final DirectoryItemRootNode rootNode;

		public DirectoryItemNodeConverter(DirectoryItemRootNode rootNode) {
			this.rootNode = rootNode;
		}

		@Override
		public String toString(DirectoryItemNode value) throws ConversionException {
			try {
				return value.file.getCanonicalPath();
			} catch (IOException e) {
				throw new ConversionException(e);
			}
		}

		@Override
		public DirectoryItemNode valueOf(String str) throws ConversionException {
			return new DirectoryItemNode(rootNode, new File(str));
		}

	}

	public static class DirectoryItemRootNode extends AbstractModifiableSubTreeNode<DirectoryItemRootNode, DirectoryItemNode, File> {
		private static class Factory implements SubTreeNodeFactory<DirectoryItemRootNode, DirectoryItemNode, File> {
			private DirectoryItemRootNode rootNode;

			@Override
			public DirectoryItemNode create(File element) {
				return new DirectoryItemNode(rootNode, element);
			}

			@Override
			public void setParent(DirectoryItemRootNode node) {
				rootNode = node;
			}
		}

		private final IWorkspaceRoot workspace;

		public DirectoryItemRootNode(IWorkspaceRoot workspace) {
			super(new Factory());
			this.workspace = workspace;
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
		protected DirectoryItemNode[] createChildrenArray() {
			return new DirectoryItemNode[] {};
		}

		@Override
		protected DirectoryItemRootNode getSelf() {
			return this;
		}
	}

	public static abstract class FileNode<P extends Node, C extends FileNode> extends AbstractSubTreeNode<C> {
		protected interface ChildNodeFactory<C extends FileNode> {
			public C createNode(File file);

			public void setParent(FileNode parent);
		}

		protected final P parent;

		protected final File file;

		protected FileNode(P parent, File file, ChildNodeFactory<C> factory) {
			this.parent = parent;
			this.file = file;
			factory.setParent(this);
			if (file.exists() && file.isDirectory()) {
				for (File f : file.listFiles()) {
					childs.add(factory.createNode(f));
				}
			}
		}

		public File getFile() {
			return file;
		}

		@Override
		public Image getImage() {
			if (file.isDirectory()) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public Node getParent() {
			return parent;
		}

		@Override
		public String getSerializeKey() throws SerializeFailedException {
			try {
				return file.getCanonicalPath();
			} catch (IOException e) {
				throw new SerializeFailedException(e);
			}
		}

	}

	public static class ViewFileNode extends AbstractViewFileNode {
		protected ViewFileNode(FileNode parent, File file) {
			super(parent, file);
		}

		@Override
		public void buildLabel(StyledString styledString) {
			styledString.append(getName(), StyledString.QUALIFIER_STYLER);
		}

		@Override
		protected ViewFileNode[] createChildrenArray() {
			return new ViewFileNode[] {};
		}
	}

	private static class Factory implements ChildNodeFactory<ViewFileNode> {
		protected FileNode parent;

		@Override
		public ViewFileNode createNode(File file) {
			return new ViewFileNode(parent, file);
		}

		@Override
		public void setParent(FileNode parent) {
			this.parent = parent;
		}

	}

}
