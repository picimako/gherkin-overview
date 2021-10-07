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

import static com.picimako.gherkin.BDDUtil.isStoryLanguageSupported;
import static com.picimako.gherkin.GherkinUtil.collectGherkinFilesFromProject;
import static com.picimako.gherkin.toolwindow.TagNameUtil.metaNameFrom;
import static com.picimako.gherkin.toolwindow.TagNameUtil.tagNameFrom;
import static java.util.stream.Collectors.toList;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

import com.intellij.execution.util.StringWithNewLinesCellEditor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.TableView;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ElementProducer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.JBehaveStoryService;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;

/**
 * Builds a panel in which tags and their mappings to categories can be collected from the current project.
 * <p>
 * The panel consists of a button that would trigger the collection process, a two-column table to display the
 * current mappings (for unmapped tags no category value is displayed), and a notification label that is
 * shown when the collection process is triggered when the IDE is still running indexing.
 * <p>
 * Tags for each category are sorted alphabetically, and displayed in an expandable textfield.
 * <p>
 * The table is not editable, all modification options are disabled.
 * <p>
 * Cells in the {@code Mapped Category} column are not editable, while ones in the {@code Tags} column are editable
 * but changes are not saved because these values are not stored, and disposed when the Settings window is closed.
 */
final class CollectGherkinTagsPanelBuilder {

    private final Project project;
    private final JButton collectGherkinTagsButton = new JButton(GherkinBundle.settings("collect.mappings.button.text"));
    private final JBLabel cannotCollectWhileIDEIsIndexingLabel = new JBLabel(GherkinBundle.settings("collect.mappings.cannot.collect"));
    private JPanel collectedMappingsPanel;
    private ListTableModel<CategoryAndTags> tableModel;

    CollectGherkinTagsPanelBuilder(@NotNull Project project) {
        this.project = project;
        cannotCollectWhileIDEIsIndexingLabel.setVisible(false);
        cannotCollectWhileIDEIsIndexingLabel.setIcon(AllIcons.General.Warning);

        collectGherkinTagsButton.addActionListener(e -> {
            if (DumbService.isDumb(project)) {
                cannotCollectWhileIDEIsIndexingLabel.setVisible(true);
                return;
            }
            cannotCollectWhileIDEIsIndexingLabel.setVisible(false);
            tableModel.setItems(collectMappingsFromProject());
            collectedMappingsPanel.setVisible(true);
        });
    }

    JPanel buildPanel() {
        collectedMappingsPanel = ToolbarDecorator.createDecorator(buildTableView(), noopElementProducer())
            .disableAddAction()
            .disableRemoveAction()
            .disableUpDownActions()
            .createPanel();
        collectedMappingsPanel.setPreferredSize(new Dimension(collectedMappingsPanel.getWidth(), 250));
        collectedMappingsPanel.setVisible(false);

        return FormBuilder.createFormBuilder()
            .addComponent(collectGherkinTagsButton)
            .addComponent(collectedMappingsPanel)
            .addComponent(cannotCollectWhileIDEIsIndexingLabel)
            .getPanel();
    }

    private ElementProducer<CategoryAndTags> noopElementProducer() {
        return new ElementProducer<>() {
            @Override
            public CategoryAndTags createElement() {
                return null;
            }

            @Override
            public boolean canCreateElement() {
                return false;
            }
        };
    }

    private TableView<CategoryAndTags> buildTableView() {
        final ColumnInfo<CategoryAndTags, String> tagsColumn = new ColumnInfo<>(GherkinBundle.settings("table.column.tags")) {
            @Override
            public @Nullable String valueOf(CategoryAndTags categoryAndTags) {
                return categoryAndTags.getTags();
            }

            @Override
            public void setValue(CategoryAndTags categoryAndTag, @NlsContexts.ListItem String value) {
                //No-op because the cell values in this table are not meant to be persisted
            }

            @Override
            public TableCellEditor getEditor(CategoryAndTags variable) {
                return new ExpandableCellEditor();
            }

            @Override
            public boolean isCellEditable(CategoryAndTags categoryAndTags) {
                return true;
            }
        };

        final ColumnInfo<CategoryAndTags, String> mappedCategoryColumn = new ColumnInfo<>(GherkinBundle.settings("table.column.mapped.category")) {
            @Override
            public String valueOf(CategoryAndTags categoryAndTag) {
                return categoryAndTag.getCategory();
            }

            @Override
            public void setValue(CategoryAndTags categoryAndTag, @NlsContexts.ListItem String value) {
                //No-op because the cell values in this table are not meant to be persisted
            }

            @Override
            public TableCellEditor getEditor(CategoryAndTags variable) {
                return new StringWithNewLinesCellEditor();
            }
        };

        return new TableView<>(tableModel = new ListTableModel<>(tagsColumn, mappedCategoryColumn));
    }

    private List<CategoryAndTags> collectMappingsFromProject() {
        var registry = TagCategoryRegistry.getInstance(project);
        final MultiMap<String, String> rawMappings = MultiMap.createOrderedSet();

        //This doesn't wait to be in Smart mode because this panel allows the collection of tags only when in Smart mode
        collectGherkinFilesFromProject(project).stream()
            .flatMap(file -> PsiTreeUtil.findChildrenOfType(file, GherkinTag.class).stream())
            .forEach(gherkinTag -> {
                String tagName = tagNameFrom(gherkinTag);
                rawMappings.putValue(registry.categoryOf(tagName), tagName);
            });

        if (isStoryLanguageSupported()) {
            //This doesn't wait to be in Smart mode because this panel allows the collection of tags only when in Smart mode
            JBehaveStoryService storyService = project.getService(JBehaveStoryService.class);
            storyService.collectStoryFilesFromProject().stream()
                .map(storyService::collectMetasFromFile)
                .flatMap(metas -> metas.entrySet().stream())
                .forEach(meta -> {
                    String metaName = metaNameFrom(meta.getKey(), meta.getValue());
                    rawMappings.putValue(registry.categoryOf(metaName), metaName);
                });
        }

        if (!rawMappings.isEmpty()) {
            //Sort the list of tags alphabetically for each category
            rawMappings.keySet().forEach(category -> Collections.sort((List<String>) rawMappings.get(category)));
            //Join the list tags by comma for each category
            return rawMappings.entrySet().stream()
                .map(entry -> new CategoryAndTags(entry.getKey(), String.join(",", entry.getValue())))
                .collect(toList());
        }
        return Collections.emptyList();
    }
}
