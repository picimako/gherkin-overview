//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the project specific settings for the Gherkin Tags tool window.
 *
 * @since 0.1.0
 */
@State(
    name = "Gherkin Overview Tags Tool Window Settings",
    storages = {@Storage("GherkinTagsToolWindowSettings.xml")}
)
@Service(Service.Level.PROJECT)
public final class GherkinTagsToolWindowSettings implements PersistentStateComponent<GherkinTagsToolWindowSettings> {

    /**
     * Stores the type of statistics to display in the tool window.
     *
     * @since 0.1.0
     */
    public StatisticsType statisticsType = StatisticsType.DISABLED;

    /**
     * Whether to show data grouped.
     *
     * @since 0.1.0
     */
    public LayoutType layout = LayoutType.NO_GROUPING;

    public static GherkinTagsToolWindowSettings getInstance(@NotNull Project project) {
        return project.getService(GherkinTagsToolWindowSettings.class);
    }

    @Override
    public @Nullable GherkinTagsToolWindowSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GherkinTagsToolWindowSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
