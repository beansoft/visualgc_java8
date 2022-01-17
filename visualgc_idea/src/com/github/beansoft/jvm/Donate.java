package com.github.beansoft.jvm;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import icons.PluginIcons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Donate {
	private static final Logger LOG = Logger.getInstance(Donate.class);

	public static JComponent newDonateButton(JPanel donatePanel) {
		JButton donate = new JButton();
		donate.setBorder(null);
		donate.setIcon(PluginIcons.Donate);
		donate.setContentAreaFilled(true);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.browse("https://plugins.jetbrains.com/plugin/14557-visualgc/pricing");
			}
		});
		donate.putClientProperty("JButton.backgroundColor", donatePanel.getBackground());
		return donate;
	}
}
