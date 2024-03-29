package com.github.beansoft.jvm.integration;

import com.github.beansoft.jvm.Hacks;
import com.github.beansoft.jvm.LogHelper;
import com.github.beansoft.jvm.action.StartVisualGCConsoleAction;
import com.intellij.execution.CantRunException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.Nullable;

public class VisualGCJavaProgramPatcher extends JavaProgramPatcher {
	private static final Logger log = Logger.getInstance(VisualGCJavaProgramPatcher.class.getName());
	long lastExecution;

	@Override
	public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
		LogHelper.print("#patchJavaParameters start", this);

		String name = configuration.getClass().getName();
		if (name.startsWith(Hacks.BUNDLED_SERVERS_RUN_PROFILE)) {
			LogHelper.print("patchJavaParameters " + name, this);

			if (System.currentTimeMillis() - lastExecution > 1000) {
				LogHelper.print("patchJavaParameters " + name + " patching", this);

				VisualGCContext visualGCContext = patch(configuration, javaParameters);
				StartVisualGCConsoleAction.setVisualGCContextToRecentlyCreated(visualGCContext);
				lastExecution = System.currentTimeMillis();
			}
		} else {
			patch(configuration, javaParameters);
		}
	}

	private VisualGCContext patch(RunProfile configuration, JavaParameters javaParameters) {
		String jdkPath = getJdkPath(javaParameters);

		final Long appId = VisualGCHelper.getNextID();
		LogHelper.print("Patching: jdkPath=" + jdkPath + "; appId=" + appId, this);
		javaParameters.getVMParametersList().prepend("-Dvisualgc.id=" + appId);


		VisualGCContext visualGCContext = new VisualGCContext(appId);
		visualGCContext.save();
		return visualGCContext;
	}

	@Nullable
	public static String getJdkPath(JavaParameters javaParameters) {
		String jdkPath = null;
		try {
			if (javaParameters.getJdk() != null && javaParameters.getJdk().getHomeDirectory() != null) {
				Sdk jdk = javaParameters.getJdk();
				SdkTypeId sdkType = jdk.getSdkType();
				if ("JavaSDK".equals(sdkType.getName())) {
					jdkPath = javaParameters.getJdkPath();
				} else if ("IDEA JDK".equals(sdkType.getName())) {
					ProjectJdkImpl jdk1 = (ProjectJdkImpl) javaParameters.getJdk();
					SdkAdditionalData sdkAdditionalData = jdk1.getSdkAdditionalData();
					if (sdkAdditionalData != null) {
						Object javaSdk = MethodUtils.invokeMethod(sdkAdditionalData, true, "getJavaSdk");
						if (javaSdk instanceof Sdk) {
							jdkPath = ((Sdk) javaSdk).getHomePath();
						}
					}
				}
			}
		} catch (CantRunException e) {
			// return;
		} catch (Throwable e) {
			log.error(e);
		}
		return jdkPath;
	}

	@SuppressWarnings("rawtypes")
	public static Module resolveModule(RunProfile runProfile) {
		Module runConfigurationModule = null;
		if (runProfile instanceof ModuleBasedConfiguration) {
			ModuleBasedConfiguration configuration = (ModuleBasedConfiguration) runProfile;
			RunConfigurationModule configurationModule = configuration.getConfigurationModule();
			if (configurationModule != null) {
				runConfigurationModule = configurationModule.getModule();
			}
		}
		return runConfigurationModule;
	}

}
