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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
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
    name = "com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings",
    storages = {@Storage("GherkinTagsToolWindowSettings.xml")}
)
@Service
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
        return ServiceManager.getService(project, GherkinTagsToolWindowSettings.class);
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
