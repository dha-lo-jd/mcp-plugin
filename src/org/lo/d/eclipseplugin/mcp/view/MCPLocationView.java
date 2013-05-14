package org.lo.d.eclipseplugin.mcp.view;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ListenerProperty;
import org.lo.d.eclipseplugin.mcp.model.AbstractPropertyListenerModel.ValueReceiver;
import org.lo.d.eclipseplugin.mcp.model.MCPPropertyModel;
import org.lo.d.eclipseplugin.mcp.resource.support.ProjectPathResolver;

public class MCPLocationView extends Composite {

	private class Reciever implements ValueReceiver<String> {
		private final Text text;

		private Reciever(Text text) {
			this.text = text;
		}

		@Override
		public void add(String value) {
			if (text.getText().equals(value)) {
				return;
			}
			text.setText(value);
		}

		@Override
		public void remove(String value) {
		}

	}

	private static class ResolevePathListener implements ModifyListener {
		private final Text text;
		private final Text pathText;
		private final IProject project;

		private ResolevePathListener(Text text, Text pathText, IProject project) {
			this.text = text;
			this.pathText = pathText;
			this.project = project;
		}

		@Override
		public void modifyText(ModifyEvent event) {
			File file = null;
			try {
				file = ProjectPathResolver.getResolvedFile(text.getText(), project);
			} catch (JavaModelException e) {
			} catch (URISyntaxException e) {
			}
			if (file != null && file.isAbsolute()) {
				pathText.setText(file.getAbsolutePath());
			} else {
				pathText.setText("不正なパス");
			}
		}

	}

	private static class TextModifyListener implements ModifyListener {
		private final Text text;

		private final ListenerProperty<String> property;

		private TextModifyListener(Text text, ListenerProperty<String> property) {
			this.text = text;
			this.property = property;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			property.add(text.getText());
		}

	}

	public MCPLocationView(Composite parent, int style, IProject project, MCPPropertyModel property) {
		super(parent, style);

		init();

		createPathPropertyResolveView(this, "MCPロケーションのパス", property.getMcpLocation(), project);
		addSeparator();
		createPathPropertyResolveView(this, "ビルド用仮想MCPロケーションのパス", property.getGenerateTempBuildLocation(), project);
		addSeparator();
		createOutputFileView(this, property.getOutputFileName());

	}

	private Label addSeparator() {
		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.WRAP);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
		return separator;
	}

	private void createOutputFileView(Composite parent, ListenerProperty<String> property) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		Display display = Display.getCurrent();
		composite.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Zip圧縮出力ファイル名");
		Color colorWhite = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		label.setBackground(colorWhite);

		Text locationText = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
		locationText.setBackground(colorWhite);
		locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		locationText.addModifyListener(new TextModifyListener(locationText, property));

		property.addReceiver(new Reciever(locationText));
	}

	private void createPathPropertyResolveView(Composite parent, final String labelText, final ListenerProperty<String> property, IProject project) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		Display display = Display.getCurrent();
		composite.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		Color colorWhite = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		label.setBackground(colorWhite);

		Text locationText = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
		locationText.setBackground(colorWhite);
		locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData buttonData = new GridData();
		buttonData.widthHint = 150;
		Button directoryButton = new Button(composite, SWT.NONE);
		directoryButton.setText("フォルダを参照");
		directoryButton.setLayoutData(buttonData);

		Label resolvePathLabel = new Label(composite, SWT.NONE);
		resolvePathLabel.setText("ファイルシステム上のパス");
		final Text resolvePathText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		resolvePathText.setBackground(colorWhite);
		resolvePathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		locationText.addModifyListener(new TextModifyListener(locationText, property));
		locationText.addModifyListener(new ResolevePathListener(locationText, resolvePathText, project));

		property.addReceiver(new Reciever(locationText));

		directoryButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setText("フォルダの選択");
				dialog.setMessage(labelText);
				dialog.setFilterPath(resolvePathText.getText());
				String path = dialog.open();

				if (path != null && !path.isEmpty()) {
					property.add(path);
				}
			}
		});
	}

	private void init() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		setLayout(layout);
		Display display = Display.getCurrent();
		setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		setLayoutData(data);
	}
}
