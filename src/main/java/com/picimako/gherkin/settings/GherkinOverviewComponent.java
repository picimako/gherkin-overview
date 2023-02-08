//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import java.util.List;
import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.picimako.gherkin.BDDUtil;
import com.picimako.gherkin.resources.GherkinBundle;

/**
 * Assembles and handles the UI panel for the settings of this plugin.
 *
 * @since 0.1.0
 */
public class GherkinOverviewComponent {

    private final JPanel settingsPanel;
    private final GherkinMappingsTable applicationLevelMappingsTable = new GherkinMappingsTable();
    private final JBCheckBox useProjectLevelMappingsCheckbox = new JBCheckBox(GherkinBundle.settings("use.project.level.mappings"));
    private final GherkinMappingsTable projectLevelMappingsTable = new GherkinMappingsTable().init();

    private final Project project;

    /**
     * Builds the Settings panel and populates the mappings tables with the proper data, and sets the UI elements to
     * their proper states.
     * <p>
     * Since there are default application level mappings (see {@code resources/mapping/default_app_level_mappings.properties}),
     * at first launch they are loaded from the aforementioned properties file, then if the Settings have been saved,
     * they will be loaded from the actual IDE settings.
     *
     * @param appSettingsMappings     the application level mappings loaded from the IDE settings
     * @param projectSettingsMappings the project level mappings loaded from the project settings
     */
    public GherkinOverviewComponent(@Nullable List<CategoryAndTags> appSettingsMappings, List<CategoryAndTags> projectSettingsMappings, Project project) {
        this.project = project;
        applicationLevelMappingsTable.withExtraActions(() ->
            new AnActionButton[]{
                //Reset to defaults
                new ExtraActionButton(GherkinBundle.settings("reset.mappings.name"), GherkinBundle.settings("reset.mappings.description"),
                    AllIcons.General.Reset,
                    () -> applicationLevelMappingsTable.setValues(DefaultMappingsLoader.loadDefaultApplicationLevelMappings()))
            }).init();
        setDefaultComponentStates();
        addProjectLevelMappingListeners();
        populateMappingTableContents(appSettingsMappings, projectSettingsMappings);
        settingsPanel = buildSettingsPanel();
    }

    private void setDefaultComponentStates() {
        if (useProjectLevelMappingsCheckbox.isSelected()) {
            projectLevelMappingsTable.setEnabled();
        } else {
            projectLevelMappingsTable.setDisabled();
        }
    }

    /**
     * When the "Use project level category-tag mapping" checkbox is ticked, the project-level mappings table is
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
     * The application-level table is populated with the default mapping data based on the
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
     * Builds the UI of the Gherkin Overview Settings panel that is separated into Application-level and Project-level
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
        var rootPathsHelpLabel = new ContextHelpLabel("",
            GherkinBundle.settings(BDDUtil.isStoryLanguageSupported()
                ? "mappings.help.description.gherkin.and.jbehave"
                : "mappings.help.description.gherkin"));
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

    // ---- Extra table actions ----

    public static final class ExtraActionButton extends AnActionButton {
        private final Runnable action;

        public ExtraActionButton(@NlsContexts.Button String text, @NlsContexts.Tooltip String description, @Nullable Icon icon, Runnable action) {
            super(text, description, icon);
            this.action = action;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            action.run();
        }
    }
}
