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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    name = "com.picimako.gherkin.settings.GherkinOverviewApplicationState",
    storages = {@Storage("GherkinOverviewAppSettings.xml")}
)
public class GherkinOverviewApplicationState implements PersistentStateComponent<GherkinOverviewApplicationState> {

    /**
     * Contains the application level category to tags mappings.
     * By default it is initialized with a default set of category-tags mappings.
     * <p>
     * Although multiple instances of this class are instantiated by the IntelliJ platform, for some reason, it is the
     * first that is actually returned when requesting an instance of this service.
     * <p>
     * The multiple instantiation happens regardless of having an application level .xml saved or not, but in the former
     * case there is one more instance of instantiation.
     *
     * @since 0.1.0
     */
    public List<CategoryAndTags> mappings = loadDefaultApplicationLevelMappings();

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

    private List<CategoryAndTags> loadDefaultApplicationLevelMappings() {
        final var categoriesAndTags = new ArrayList<CategoryAndTags>();
        try (InputStream mappingsResource = GherkinOverviewComponent.class.getResourceAsStream("/mapping/default_app_level_mappings.properties")) {
            var defaultAppLevelMappings = new Properties();
            defaultAppLevelMappings.load(mappingsResource);
            defaultAppLevelMappings.forEach((key, value) -> categoriesAndTags.add(new CategoryAndTags(key.toString(), value.toString())));
        } catch (IOException ioException) {
            throw new RuntimeException("Could not load default Gherkin category-tag mappings.", ioException);
        }
        return categoriesAndTags;
    }
}
