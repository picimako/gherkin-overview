//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for retrieving data related to the Gherkin tag tool window.
 */
public final class GherkinTagToolWindowUtil {

    private static final Logger LOG = Logger.getInstance(GherkinTagToolWindowUtil.class);
    private static final String TOOL_WINDOW_ID = "gherkin.overview.tool.window.id";

    /**
     * Returns the Gherkin tag tool window for the current project,
     * or throws an exception if the tool window is not registered.
     *
     * @param project the current project
     * @return the tool window
     */
    @Nullable
    public static ToolWindow getGherkinTagsToolWindow(@NotNull Project project) {
        ToolWindow gherkinTagsToolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (gherkinTagsToolWindow == null) {
            LOG.error("There is no tool window registered with the id: [" + TOOL_WINDOW_ID + "]");
            return null;
        }
        return gherkinTagsToolWindow;
    }

    /**
     * Returns the tool window hider component for the provided tool window.
     *
     * @param gherkinTagsToolWindow the tool window
     * @return the tool window hider
     */
    @NotNull
    public static GherkinTagToolWindowHider getToolWindowHider(@NotNull ToolWindow gherkinTagsToolWindow) {
        return (GherkinTagToolWindowHider) gherkinTagsToolWindow
            .getContentManager()
            //This works as long as the one with "gherkin.overview.tool.window.id" id is the first content in the tool window
            .getContent(0)
            .getComponent();
    }

    public static GherkinTagOverviewPanel getGherkinTagOverViewPanel(@NotNull ToolWindow gherkinTagsToolWindow) {
        var hider = getToolWindowHider(gherkinTagsToolWindow);
        return (GherkinTagOverviewPanel) hider.getComponent(0);
    }

    private GherkinTagToolWindowUtil() {
        //Utility class
    }
}
