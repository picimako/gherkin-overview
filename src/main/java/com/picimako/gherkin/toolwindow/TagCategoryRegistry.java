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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.settings.GherkinOverviewApplicationState;
import com.picimako.gherkin.settings.GherkinOverviewProjectState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Project service to store the mappings between tag and category names. Each tag name is assigned to a category and stored as a separate
 * entry. The following assignments in the plugin Settings
 * <pre>
 * Test Suite=smoke,regression,e2e
 * Device=mobile, tablet, desktop
 * </pre>
 * will be stored in the underlying mapping as:
 * <pre>
 * smoke -> Test Suite
 * regression -> Test Suite
 * e2e -> Test Suite
 * mobile -> Device
 * tablet -> Device
 * desktop -> Device
 * </pre>
 * This service is initialized when the tool window's {@link GherkinTagTreeModel} is being first built and it requests
 * a mapping by calling {@link #categoryOf(String)}.
 * <p>
 * During initialization, tag names are trimmed to allow storing them in the app and project level
 * settings, with a leading whitespace, e.g. {@code smoke, regression, e2e} instead of {@code smoke,regression,e2e}.
 * <p>
 * Tag names are stored in a case-sensitive way (may change in the future) and without the leading @ character.
 *
 * <h2>Regex based tags</h2>
 * It is not just exact tag names that can be assigned to categories but regex patterns as well. They must be defined
 * starting with the # symbol to have them recognized as regex patterns.
 * <p>
 * One example is Jira ticket ids in which case the mapping looks like
 * <pre>
 * Jira=#^[A-Z]+-[0-9]+$
 * </pre>
 * <p>
 * So, any tag name that matches this pattern will be added to the Jira category. If there is more than one category
 * this same pattern, or one yielding the same matches is added to the registry, upon querying the one added earliest
 * is returned and used as the tag's category.
 *
 * @since 0.1.0
 */
@Service
public final class TagCategoryRegistry {

    private static final String TAG_DELIMITER = ",";
    /**
     * Tag name -> category name.
     * <p>
     * Initial capacity is adjusted slightly above the default mapping count coming from the {@code default_app_level_mappings.properties}
     * default mappings.
     */
    private final Map<String, String> tagToCategory = new HashMap<>(64);

    /**
     * Here the registry gets initialized with the values from the application and project level mappings. By doing this
     * the initialization of this registry doesn't depend on going into the Settings to instantiate the services, rather
     * the initialization of them happens here.
     * <p>
     * Application-level mappings are added first, so if they need to be overridden by project-level ones,
     * it can be done properly.
     */
    public TagCategoryRegistry(Project project) {
        putMappingsFrom(GherkinOverviewApplicationState.getInstance().mappings);
        var projectSettings = GherkinOverviewProjectState.getInstance(project);
        if (projectSettings.useProjectLevelMappings) {
            putMappingsFrom(projectSettings.mappings);
        }
    }

    /**
     * Removes all mappings from this registry.
     */
    public void clearMappings() {
        tagToCategory.clear();
    }

    /**
     * Adds all mappings from the argument to this registry.
     *
     * @param categoryAndTags the mappings to store
     */
    public void putMappingsFrom(@NotNull List<CategoryAndTags> categoryAndTags) {
        for (var cat : categoryAndTags) {
            for (String tag : cat.getTags().split(TAG_DELIMITER)) {
                tagToCategory.put(tag.trim(), cat.getCategory());
            }
        }
    }

    /**
     * Returns the category that the argument tag name is assigned to.
     * <p>
     * If the tag name, as an exact value, is not assigned to any category, it might be a regex based value, so will
     * try to find to proper category based on that. If no category is found, it returns null.
     *
     * @param tagName the tag name
     */
    @Nullable
    public String categoryOf(String tagName) {
        return Optional.ofNullable(tagToCategory.get(tagName)).orElseGet(() -> regexBasedCategoryOf(tagName));
    }

    @Nullable
    private String regexBasedCategoryOf(String tagName) {
        return tagToCategory.keySet().stream()
            .filter(key -> key.startsWith("#") && tagName.matches(key.substring(1)))
            .map(tagToCategory::get)
            .findFirst()
            .orElse(null);
    }

    public static TagCategoryRegistry getInstance(Project project) {
        return project.getService(TagCategoryRegistry.class);
    }
}
