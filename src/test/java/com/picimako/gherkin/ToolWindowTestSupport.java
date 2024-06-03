//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getToolWindowHider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagToolWindowFactory;
import com.picimako.gherkin.toolwindow.GherkinTagToolWindowHider;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import kotlin.Unit;

import javax.swing.*;

/**
 * Utility for managing tool windows in unit tests.
 */
public final class ToolWindowTestSupport {

    private static final String TOOL_WINDOW_ID = "gherkin.overview.tool.window.id";

    public static void registerToolWindow(Project project) {
        registerToolWindow(new JPanel(), project);
    }

    public static void registerToolWindow(JPanel panelContent, Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_ID, taskBuilder -> {
            taskBuilder.anchor = ToolWindowAnchor.LEFT;
            taskBuilder.sideTool = true;
            taskBuilder.canCloseContent = true;
            taskBuilder.shouldBeAvailable = true;
            taskBuilder.contentFactory = new GherkinTagToolWindowFactory();
            taskBuilder.icon = null;
            taskBuilder.stripeTitle = null;
            return Unit.INSTANCE;
        });
        Content content = toolWindow.getContentManager().getFactory()
            .createContent(new GherkinTagToolWindowHider(panelContent, project), "", true);
        toolWindow.getContentManager().addContent(content);
    }

    public static ModelDataRoot getToolWindowModel(Project project) {
        GherkinTagToolWindowHider hider = getToolWindowHider(ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID));
        GherkinTagOverviewPanel toolWindowPanel = (GherkinTagOverviewPanel) hider.getComponent(0);
        return (ModelDataRoot) toolWindowPanel.getTree().getModel().getRoot();
    }

    private ToolWindowTestSupport() {
        //Utility class
    }
}
