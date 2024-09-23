//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import java.util.List;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component for storing the application (IDE) level plugin settings.
 * <p>
 * For the project level settings see {@link GherkinOverviewProjectState}.
 * 
 * @since 0.1.0
 */
@State(
    name = "Gherkin Overview Application-level Mappings",
    storages = {@Storage(value = "GherkinOverviewAppSettings.xml", exportable = true)}
)
public final class GherkinOverviewApplicationState implements PersistentStateComponent<GherkinOverviewApplicationState> {

    /**
     * Contains the application level category to tags mappings.
     * By default, it is initialized with a default set of category-tags mappings.
     * <p>
     * Although multiple instances of this class are instantiated by the IntelliJ platform, for some reason, it is the
     * first that is actually returned when requesting an instance of this service.
     * <p>
     * The multiple instantiation happens regardless of having an application level .xml saved or not, but in the former
     * case there is one more instance of instantiation.
     *
     * @since 0.1.0
     */
    public List<CategoryAndTags> mappings = DefaultMappingsLoader.loadDefaultApplicationLevelMappings();

    public static GherkinOverviewApplicationState getInstance() {
        return ApplicationManager.getApplication().getService(GherkinOverviewApplicationState.class);
    }

    @Override
    public @Nullable GherkinOverviewApplicationState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GherkinOverviewApplicationState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
