package com.github.beansoft.jvm;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;

public class SettingsDialog {
	private JComponent rootComponent;

	private JFormattedTextField duration;

	private JFormattedTextField delayForStgartingVisualVM;

	private JPanel donatePanel;
	private JLabel durationLabel;


	public SettingsDialog() {
		super();
		donatePanel.add(Donate.newDonateButton(donatePanel));
		duration.setFormatterFactory(getDefaultFormatterFactory());
		delayForStgartingVisualVM.setFormatterFactory(getDefaultFormatterFactory());
	}

	private DefaultFormatterFactory getDefaultFormatterFactory() {
		NumberFormatter defaultFormat = new NumberFormatter();
		NumberFormat integerInstance = NumberFormat.getIntegerInstance();
		integerInstance.setGroupingUsed(false);
		defaultFormat.setFormat(integerInstance
		);
		return new DefaultFormatterFactory(defaultFormat);
	}

	private void browseForFile(@NotNull final JTextField target) {
		final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
		descriptor.setHideIgnored(true);

		descriptor.setTitle("Select VisualVM Executable");
		String text = target.getText();
		final VirtualFile toSelect = text == null || text.isEmpty() ? null
				: LocalFileSystem.getInstance().findFileByPath(text);

		// 10.5 does not have #chooseFile
		Project defaultProject = ProjectManager.getInstance().getDefaultProject();
		VirtualFile[] virtualFile = FileChooser.chooseFiles(descriptor, defaultProject, toSelect);
		if (virtualFile != null && virtualFile.length > 0) {
			target.setText(virtualFile[0].getPath());
		}
	}


	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setDataCustom(PluginSettings settings) {
		setData(settings);
	}

	public void setData(PluginSettings data) {

		duration.setText(data.getDurationToSetContextToButton());
		delayForStgartingVisualVM.setText(data.getDelayForVisualVMStart());
	}

	public void getData(PluginSettings data) {
		data.setDurationToSetContextToButton(duration.getText());
		data.setDelayForVisualVMStart(delayForStgartingVisualVM.getText());
	}

	public boolean isModified(PluginSettings data) {
		if (duration.getText() != null ? !duration.getText().equals(data.getDurationToSetContextToButton()) : data.getDurationToSetContextToButton() != null)
			return true;
		if (delayForStgartingVisualVM.getText() != null ? !delayForStgartingVisualVM.getText().equals(data.getDelayForVisualVMStart()) : data.getDelayForVisualVMStart() != null)
			return true;

		return false;
	}
}
