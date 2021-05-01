/*
 * Copyright 2021 Tamás Balog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.picimako.gherkin.settings;

import java.util.ArrayList;
import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

/**
 * A Configurable object acting as the bridge between the Gherkin Overview Settings UI components,
 * the IDE level settings and the project level settings.
 * <p>
 * Creating a new ArrayList in this class is to avoid writing to the collection instance stored by the application and
 * project services, before actually saving the form, or during resetting to assign the object references from the
 * services to the UI component.
 */
public class GherkinOverviewProjectConfigurable implements Configurable {

    private final Project project;
    private GherkinOverviewComponent component;

    public GherkinOverviewProjectConfigurable(Project project) {
        this.project = project;
    }

    @NlsContexts.ConfigurableName
    @Override
    public String getDisplayName() {
        return "Gherkin Overview Plugin Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        var appSettingsMappings = GherkinOverviewApplicationState.getInstance().mappings;
        var projectSettingsMappings = GherkinOverviewProjectState.getInstance(project).mappings;
        component = new GherkinOverviewComponent(appSettingsMappings, projectSettingsMappings, project);
        return component.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        var appSettingsMappings = GherkinOverviewApplicationState.getInstance().mappings;
        var projectSettings = GherkinOverviewProjectState.getInstance(project);

        return projectSettings.useProjectLevelMappings != component.isUseProjectLevelMappings()
            || !component.getApplicationLevelMappings().equals(appSettingsMappings)
            || !component.getProjectLevelMappings().equals(projectSettings.mappings);
    }

    @Override
    public void apply() throws ConfigurationException {
        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(project);

        appSettings.mappings = new ArrayList<>(component.getApplicationLevelMappings());
        var registry = TagCategoryRegistry.getInstance(project);
        registry.clearMappings();
        registry.putMappingsFrom(appSettings.mappings);
        if (component.isUseProjectLevelMappings()) {
            registry.putMappingsFrom(component.getProjectLevelMappings());
        }

        projectSettings.useProjectLevelMappings = component.isUseProjectLevelMappings();
        var projectLevelMappings = component.getProjectLevelMappings();
        projectSettings.mappings = new ArrayList<>(projectLevelMappings);
    }

    @Override
    public void reset() {
        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(project);
        component.setApplicationLevelMappings(new ArrayList<>(appSettings.mappings));
        component.setUseProjectLevelMappings(projectSettings.useProjectLevelMappings);
        component.setProjectLevelMappings(new ArrayList<>(projectSettings.mappings));
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

    @TestOnly
    GherkinOverviewComponent getComponent() {
        return component;
    }
}
