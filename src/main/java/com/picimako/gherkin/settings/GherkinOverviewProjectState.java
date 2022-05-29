//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
    name = "Gherkin Overview Project-Level Mappings",
    storages = {@Storage(value = "GherkinOverviewProjectSettings.xml", exportable = true)}
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
