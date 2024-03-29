package com.github.beansoft.jvm;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

@State(name = "VisualGCIDEA", storages = {@Storage("VisualGCIDEA.xml")})
public class ApplicationSettingsService implements PersistentStateComponent<PluginSettings> {
	private static final Logger log = Logger.getInstance(ApplicationSettingsService.class.getName());

	private PluginSettings settings = new PluginSettings();

	public static ApplicationSettingsService getInstance() {
		return ApplicationManager.getApplication().getService(ApplicationSettingsService.class);
	}

	@NotNull
	public String getComponentName() {
		return "VisualGC";
	}


	@NotNull
	@Override
	public PluginSettings getState() {
		if (settings == null) {
			settings = new PluginSettings();
		}
		return settings;
	}

	@Override
	public void loadState(PluginSettings state) {
		this.settings = state;
	}
}
