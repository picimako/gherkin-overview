/*
 *  Copyright 2021 Tamás Balog
 *
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.picimako.gherkin;

import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getToolWindowHider;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagToolWindowFactory;
import com.picimako.gherkin.toolwindow.GherkinTagToolWindowHider;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Utility for managing tool windows in unit tests.
 */
public final class ToolWindowTestSupport {

    private static final String TOOL_WINDOW_ID = "gherkin.overview.tool.window.id";

    public static void registerToolWindow(Project project) {
        registerToolWindow(new JPanel(), project);
    }

    public static void registerToolWindow(JPanel panelContent, Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
            .registerToolWindow(
                new RegisterToolWindowTask(TOOL_WINDOW_ID,
                    ToolWindowAnchor.LEFT,
                    new GherkinTagToolWindowHider(panelContent, project),
                    true, true, false, true,
                    new GherkinTagToolWindowFactory(),
                    null, null));
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
