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

import java.util.List;
import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.picimako.gherkin.resources.GherkinBundle;
import org.jetbrains.annotations.Nullable;

/**
 * Assembles and handles the UI panel for the Settings of this plugin.
 *
 * @since 0.1.0
 */
public class GherkinOverviewComponent {

    private final JPanel settingsPanel;
    private final GherkinMappingsTable applicationLevelMappingsTable = new GherkinMappingsTable();
    private final JBCheckBox useProjectLevelMappingsCheckbox = new JBCheckBox(GherkinBundle.settings("use.project.level.mappings"));
    private final GherkinMappingsTable projectLevelMappingsTable = new GherkinMappingsTable();

    private final Project project;

    /**
     * Build the Settings panel and populates the mappings tables with the proper data, and sets the UI elements to
     * their proper statuses.
     * <p>
     * Since there are default application level mappings (see {@code resources/mapping/default_app_level_mappings.properties}),
     * at first launch they are loaded from the aforementioned properties file, then if the Settings have been saved,
     * then they will be loaded from the actual IDE settings.
     * <p>
     * The {@code toLoadDefaultApplicationLevelMappings} argument is the one that tells this class whether the mapping
     * Settings have been saved or not, thus whether the default mappings have to be loaded or not.
     *
     * @param appSettingsMappings     the application level mappings loaded from the IDE settings
     * @param projectSettingsMappings the project level mappings loaded from the project settings
     */
    public GherkinOverviewComponent(@Nullable List<CategoryAndTags> appSettingsMappings, List<CategoryAndTags> projectSettingsMappings, Project project) {
        this.project = project;
        addProjectLevelMappingListeners();
        populateMappingTableContents(appSettingsMappings, projectSettingsMappings);
        settingsPanel = buildSettingsPanel();
    }

    /**
     * When the "Use project level category-tag mapping" checkbox is ticked, then the project level mappings table is
     * enabled, and when the checkbox is unticked, the table gets disabled as well.
     */
    private void addProjectLevelMappingListeners() {
        useProjectLevelMappingsCheckbox.addChangeListener(e -> {
            if (((JBCheckBox) e.getSource()).isSelected()) {
                projectLevelMappingsTable.setEnabled();
            } else {
                projectLevelMappingsTable.setDisabled();
                //To improve user experience, and not to confuse users, when the project level table is being disabled,
                // all selections are cleared
                projectLevelMappingsTable.getTableView().clearSelection();
            }
        });
    }

    /**
     * Populates the mapping tables with the proper data.
     * <p>
     * The application level table is populated with the default mapping data based on the
     * {@code appSettingsMappings} argument's value.
     *
     * @param appSettingsMappings     the application level mappings loaded from the IDE settings
     * @param projectSettingsMappings the project level mappings loaded from the project settings
     */
    private void populateMappingTableContents(@Nullable List<CategoryAndTags> appSettingsMappings, List<CategoryAndTags> projectSettingsMappings) {
        applicationLevelMappingsTable.setValues(appSettingsMappings);
        projectLevelMappingsTable.setValues(projectSettingsMappings);
    }

    /**
     * Builds the UI of the Gherkin Overview Settings panel that are separated into Application level and Project level
     * mappings.
     */
    private JPanel buildSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(GherkinBundle.settings("application.level.mappings.title")))
            .addComponent(applicationLevelMappingsTable.getComponent(), 2)
            .addVerticalGap(10)

            .addComponent(new TitledSeparator(GherkinBundle.settings("project.level.mappings.title")))
            .addComponent(useProjectLevelMappingsCheckbox, 2)
            .addVerticalGap(5)
            .addComponent(projectLevelMappingsTable.getComponent(), 2)
            .addVerticalGap(8)
            .addComponent(contextHelpLabel())

            .addComponent(new TitledSeparator(GherkinBundle.settings("tags.in.current.project")))
            .addVerticalGap(2)
            .addComponent(new CollectGherkinTagsPanelBuilder(project).buildPanel())

            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    private ContextHelpLabel contextHelpLabel() {
        var rootPathsHelpLabel = new ContextHelpLabel("", GherkinBundle.settings(BDDUtil.isStoryLanguageSupported() ? "mappings.help.description.gherkin.and.jbehave" : "mappings.help.description.gherkin"));
        rootPathsHelpLabel.setIcon(AllIcons.General.ContextHelp);
        return rootPathsHelpLabel;
    }

    // ---- Getter, setter, retriever methods ----

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    public boolean isUseProjectLevelMappings() {
        return useProjectLevelMappingsCheckbox.isSelected();
    }

    public void setUseProjectLevelMappings(boolean value) {
        useProjectLevelMappingsCheckbox.setSelected(value);
    }

    public List<CategoryAndTags> getProjectLevelMappings() {
        return projectLevelMappingsTable.getTableView().getTableViewModel().getItems();
    }

    public void setProjectLevelMappings(List<CategoryAndTags> mappings) {
        projectLevelMappingsTable.setValues(mappings);
    }

    public List<CategoryAndTags> getApplicationLevelMappings() {
        return applicationLevelMappingsTable.getTableView().getTableViewModel().getItems();
    }

    public void setApplicationLevelMappings(@Nullable List<CategoryAndTags> mappings) {
        if (mappings != null) {
            applicationLevelMappingsTable.setValues(mappings);
        }
    }
}
