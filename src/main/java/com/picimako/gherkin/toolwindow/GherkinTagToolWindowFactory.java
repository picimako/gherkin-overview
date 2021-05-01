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

import static java.util.Collections.singletonList;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the contents of the Gherkin tag tool window.
 * <p>
 * The tool window is enabled only when the following conditions are met:
 * <ul>
 *     <li>the project is not a default project</li>
 * </ul>
 * <p>
 * The overview panel is wrapped in a {@link GherkinTagToolWindowHider}, so that when there is no Gherkin tag available
 * in a project, it shows a placeholder text instead.
 *
 * @see GherkinTagOverviewPanel
 * @since 0.1.0
 */
public class GherkinTagToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //Registering in StartupManager to make sure that the indices are completely available to collect Gherkin files from the project
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            GherkinTagOverviewPanel overviewPanel = new GherkinTagOverviewPanel(project);
            GherkinTagToolWindowHider hider = new GherkinTagToolWindowHider(overviewPanel, project);

            toolWindow.setTitleActions(singletonList(new ToolWindowAppearanceActionGroupCreator(
                () -> overviewPanel.getTree().updateUI(),
                () -> overviewPanel.updateModel()
            ).create()));

            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getFactory().createContent(hider, null, true);
            contentManager.addContent(content);

            hider.setContentVisibilityBasedOn(overviewPanel.modelDataRoot());
        });
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return !project.isDefault();
    }
}
