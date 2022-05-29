//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static java.util.Collections.singletonList;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.BDDUtil;
import com.picimako.gherkin.resources.GherkinBundle;

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
        //Registering in StartupManager to make sure that the indices are completely available to collect files from the project
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            GherkinTagOverviewPanel overviewPanel = new GherkinTagOverviewPanel(project);

            toolWindow.setTitleActions(singletonList(new ToolWindowAppearanceActionGroupCreator(
                () -> overviewPanel.getTree().updateUI(),
                () -> overviewPanel.updateModel()
            ).create()));

            GherkinTagToolWindowHider hider = new GherkinTagToolWindowHider(overviewPanel, project, getHiderMessage());
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getFactory().createContent(hider, null, true);
            contentManager.addContent(content);

            hider.setContentVisibilityBasedOn(overviewPanel.modelDataRoot());
        });
    }

    @NotNull
    private String getHiderMessage() {
        return BDDUtil.isStoryLanguageSupported() ? GherkinBundle.toolWindow("no.tag.or.meta.in.project") : GherkinBundle.toolWindow("no.tag.in.project");
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return !project.isDefault();
    }

}
