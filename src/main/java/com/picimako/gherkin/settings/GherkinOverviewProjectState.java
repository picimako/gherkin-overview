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

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component for storing the project level plugin settings.
 * <p>
 * For the application (IDE) level settings see {@link GherkinOverviewApplicationState}.
 *
 * @since 0.1.0
 */
@State(
    name = "com.picimako.gherkin.settings.GherkinOverviewProjectState",
    storages = {@Storage("GherkinOverviewProjectSettings.xml")}
)
public class GherkinOverviewProjectState implements PersistentStateComponent<GherkinOverviewProjectState> {

    /**
     * Whether to override the IDE-level mappings in the current project.
     *
     * @since 0.1.0
     */
    public boolean useProjectLevelMappings = false;
    /**
     * Contains the project level category to tags mappings.
     *
     * @since 0.1.0
     */
    public List<CategoryAndTags> mappings = new ArrayList<>();

    public static GherkinOverviewProjectState getInstance(Project project) {
        return project.getService(GherkinOverviewProjectState.class);
    }

    @Override
    public @Nullable GherkinOverviewProjectState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GherkinOverviewProjectState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
