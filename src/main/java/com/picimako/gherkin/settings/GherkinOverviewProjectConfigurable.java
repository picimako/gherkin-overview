//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getGherkinTagsToolWindow;
import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getToolWindowHider;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagToolWindowHider;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * A Configurable object acting as the bridge between the Gherkin Overview Settings UI components,
 * the IDE level settings and the project level settings.
 * <p>
 * Creating a new ArrayList in this class is to avoid writing to the collection instance stored by the application and
 * project services, before actually saving the form, or during resetting to assign the object references from the
 * services to the UI component.
 * <p>
 * The whole model in the Gherkin tag tool window is rebuilt when any of the mappings changes in the Settings. This is
 * so that users don't have to restart the IDE themselves to have the data updated, and there is no need for a manual
 * "rebuild model manually" feature to be implemented in the tool window.
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
        return GherkinBundle.settings("project.configurable.display.name");
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
            || hasAnyMappingsChanged(appSettingsMappings, projectSettings.mappings);
    }

    @Override
    public void apply() throws ConfigurationException {
        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(project);
        //Saving this state, before overwriting the saved mappings with the new values
        boolean toRebuildModel = hasAnyMappingsChanged(appSettings.mappings, projectSettings.mappings);

        appSettings.mappings = new ArrayList<>(component.getApplicationLevelMappings());
        var registry = TagCategoryRegistry.getInstance(project);
        registry.dispose();
        registry.putMappingsFrom(appSettings.mappings);
        if (component.isUseProjectLevelMappings()) {
            registry.putMappingsFrom(component.getProjectLevelMappings());
        }

        projectSettings.useProjectLevelMappings = component.isUseProjectLevelMappings();
        var projectLevelMappings = component.getProjectLevelMappings();
        projectSettings.mappings = new ArrayList<>(projectLevelMappings);

        if (toRebuildModel) {
            rebuildModel();
        }
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

    private boolean hasAnyMappingsChanged(List<CategoryAndTags> appLevelMappings, List<CategoryAndTags> projectLevelMappings) {
        return !component.getApplicationLevelMappings().equals(appLevelMappings)
            || !component.getProjectLevelMappings().equals(projectLevelMappings);
    }

    /**
     * The actual UI panel and the underlying model is available via {@link GherkinTagToolWindowHider},
     * so it doesn't matter if the tool window has or hasn't been opened before, the model will be rebuilt.
     */
    private void rebuildModel() {
        ToolWindow gherkinTagsToolWindow = getGherkinTagsToolWindow(project);
        if (gherkinTagsToolWindow != null) {
            GherkinTagToolWindowHider hider = getToolWindowHider(gherkinTagsToolWindow);
            GherkinTagOverviewPanel toolWindowPanel = (GherkinTagOverviewPanel) hider.getComponent(0);
            toolWindowPanel.rebuildModel();
            hider.setContentVisibilityBasedOn((ModelDataRoot) toolWindowPanel.getTree().getModel().getRoot());
        }
    }

    @TestOnly
    GherkinOverviewComponent getComponent() {
        return component;
    }
}
