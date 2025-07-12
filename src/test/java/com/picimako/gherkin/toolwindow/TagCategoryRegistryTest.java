//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.settings.GherkinOverviewProjectState;

/**
 * Unit test for {@link TagCategoryRegistry}.
 */
public class TagCategoryRegistryTest extends GherkinOverviewTestBase {

    //TagCategoryRegistry

    public void testLoadApplicationLevelMappings() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(s -> {
            s.assertThat(registry.categoryOf("desktop")).isEqualTo("Device");
            s.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser");
            s.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO");
            s.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress");
            s.assertThat(registry.categoryOf("jumped")).isNull();
        });
    }

    public void testAddProjectLevelMappingsToApplicationLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(new CategoryAndTags("Excluded", "jumped,hopped"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(s -> {
            s.assertThat(registry.categoryOf("desktop")).isEqualTo("Device");
            s.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser");
            s.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO");
            s.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress");
            s.assertThat(registry.categoryOf("jumped")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("hopped")).isEqualTo("Excluded");
        });
    }

    public void testOverrideApplicationLevelCategoryWithProjectLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(
            new CategoryAndTags("Ignored", "ignore"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertSoftly(s -> {
            s.assertThat(registry.categoryOf("desktop")).isEqualTo("Device");
            s.assertThat(registry.categoryOf("firefox")).isEqualTo("Browser");
            s.assertThat(registry.categoryOf("skip")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("meta")).isEqualTo("Analytics and SEO");
            s.assertThat(registry.categoryOf("WIP")).isEqualTo("Work in Progress");
            s.assertThat(registry.categoryOf("ignore")).isEqualTo("Ignored");
            s.assertThat(registry.categoryOf("sanity")).isEqualTo("Test Pack");
            s.assertThat(registry.categoryOf("e2e")).isEqualTo("Test Pack");
        });
    }

    //putMappingsFrom

    public void testPutMappingsFromListOfCategoriesAndTags() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());
        var mappings = List.of(
            new CategoryAndTags("Excluded", "jumped,hopped"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        registry.putMappingsFrom(mappings);

        assertSoftly(s -> {
            s.assertThat(registry.categoryOf("jumped")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("hopped")).isEqualTo("Excluded");
            s.assertThat(registry.categoryOf("sanity")).isEqualTo("Test Pack");
            s.assertThat(registry.categoryOf("e2e")).isEqualTo("Test Pack");
        });
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
