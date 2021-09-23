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

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.settings.GherkinOverviewProjectState;

/**
 * Unit test for {@link TagCategoryRegistry}.
 *
 * TODO: fix tests due to lack of application level service cleanup. !! It is a problem only during full test suite execution.
 */
public class TagCategoryRegistryTest extends BasePlatformTestCase {

    //TagCategoryRegistry

    public void testLoadApplicationLevelMappings() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(
            softly -> softly.assertThat(registry.categoryOf("desktop")).isEqualTo("Device"),
            softly -> softly.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser"),
            softly -> softly.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO"),
            softly -> softly.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress"),
            softly -> softly.assertThat(registry.categoryOf("jumped")).isNull()
        );
    }

    public void testAddProjectLevelMappingsToApplicationLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(new CategoryAndTags("Excluded", "jumped,hopped"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(
            softly -> softly.assertThat(registry.categoryOf("desktop")).isEqualTo("Device"),
            softly -> softly.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser"),
            softly -> softly.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO"),
            softly -> softly.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress"),
            softly -> softly.assertThat(registry.categoryOf("jumped")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("hopped")).isEqualTo("Excluded")
        );
    }

    public void testOverrideApplicationLevelCategoryWithProjectLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(
            new CategoryAndTags("Ignored", "ignore"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(
            softly -> softly.assertThat(registry.categoryOf("desktop")).isEqualTo("Device"),
            softly -> softly.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser"),
            softly -> softly.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO"),
            softly -> softly.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress"),
            softly -> softly.assertThat(registry.categoryOf("ignore")).isEqualTo("Ignored"),
            softly -> softly.assertThat(registry.categoryOf("sanity")).isEqualTo("Test Pack"),
            softly -> softly.assertThat(registry.categoryOf("e2e")).isEqualTo("Test Pack")
        );
    }

    //putMappingsFrom

    public void testPutMappingsFromListOfCategoriesAndTags() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());
        var mappings = List.of(
            new CategoryAndTags("Excluded", "jumped,hopped"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        registry.putMappingsFrom(mappings);

        assertSoftly(
            softly -> softly.assertThat(registry.categoryOf("jumped")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("hopped")).isEqualTo("Excluded"),
            softly -> softly.assertThat(registry.categoryOf("sanity")).isEqualTo("Test Pack"),
            softly -> softly.assertThat(registry.categoryOf("e2e")).isEqualTo("Test Pack")
        );
    }

    //categoryOf

    public void testReturnCategoryOfExistingTag() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertThat(registry.categoryOf("desktop")).isEqualTo("Device");
    }

    public void testReturnNoCategoryForNonMappedTag() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertThat(registry.categoryOf("non-mapped")).isNull();
    }

    public void testReturnCategoryForRegexBasedTag() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertThat(registry.categoryOf("JIRA-1234")).isEqualTo("Jira");
    }
}
