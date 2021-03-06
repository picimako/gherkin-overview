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

package com.picimako.gherkin.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.picimako.gherkin.resources.GherkinBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an action group in which the appearance of the Gherkin Tags tool window can be customized.
 * <p>
 * The structure is:
 * <pre>
 * Statistics
 *      Disabled
 *      Simplified
 *      Detailed
 * Layout
 *      Group by Modules
 * </pre>
 * <p>
 * After toggling actions the model data and/or the tool window UI is updated to reflect the changes.
 */
public final class ToolWindowAppearanceActionGroupCreator {

    private final Runnable updateUICallback;
    private final Runnable updateModelCallback;

    public ToolWindowAppearanceActionGroupCreator(Runnable updateUICallback, Runnable updateModelCallback) {
        this.updateUICallback = updateUICallback;
        this.updateModelCallback = updateModelCallback;
    }

    public DefaultActionGroup create() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.getTemplatePresentation().setText(GherkinBundle.toolWindow("statistics.button.tooltip"));
        group.getTemplatePresentation().setDescription(GherkinBundle.toolWindow("statistics.button.description"));
        group.getTemplatePresentation().setIcon(AllIcons.Actions.Show);

        group.add(new Separator(GherkinBundle.toolWindow("statistics.separator")));
        group.add(createStatAction(GherkinBundle.toolWindow("statistics.type.disabled"), StatisticsType.DISABLED));
        group.add(createStatAction(GherkinBundle.toolWindow("statistics.type.simplified"), StatisticsType.SIMPLIFIED));
        group.add(createStatAction(GherkinBundle.toolWindow("statistics.type.detailed"), StatisticsType.DETAILED));

        group.add(new Separator(GherkinBundle.toolWindow("layout.separator")));
        group.add(createLayoutAction(GherkinBundle.toolWindow("layout.group.by.modules")));

        group.setPopup(true);
        return group;
    }

    private ToggleAction createStatAction(String text, StatisticsType statisticsType) {
        return new ToggleAction(text, null, null) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return e.getProject() != null && GherkinTagsToolWindowSettings.getInstance(e.getProject()).statisticsType == statisticsType;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                if (state) {
                    GherkinTagsToolWindowSettings.getInstance(e.getProject()).statisticsType = statisticsType;
                    updateUICallback.run();
                }
            }
        };
    }

    private ToggleAction createLayoutAction(String text) {
        return new ToggleAction(text, null, null) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return e.getProject() != null && GherkinTagsToolWindowSettings.getInstance(e.getProject()).layout == LayoutType.GROUP_BY_MODULES;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                GherkinTagsToolWindowSettings.getInstance(e.getProject()).layout = state ? LayoutType.GROUP_BY_MODULES : LayoutType.NO_GROUPING;
                //The UI update must happen after the model is updated
                updateModelCallback.run();
                updateUICallback.run();
            }
        };
    }
}
