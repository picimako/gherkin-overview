//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.settings.GherkinOverviewProjectState;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link TagCategoryRegistry}.
 */
final class TagCategoryRegistryTest extends GherkinOverviewTestBase {

    //TagCategoryRegistry

    @Test
    @Disabled("FIXME: the behaviour of this is influenced by other tests which makes it fail when executed in batch with others")
    void loadApplicationLevelMappings() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        //Using HashMap because of passing in a null value
        var tagsToCategories = new HashMap<String, String>();
        tagsToCategories.put("desktop", "Device");
        tagsToCategories.put("firefox", "Browser");
        tagsToCategories.put("skip", "Excluded");
        tagsToCategories.put("meta", "Analytics and SEO");
        tagsToCategories.put("WIP", "Work in Progress");
        tagsToCategories.put("jumped", null);

        assertCategoriesOfTags(registry, tagsToCategories);
    }

    @Test
    void addProjectLevelMappingsToApplicationLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(new CategoryAndTags("Excluded", "jumped,hopped"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertCategoriesOfTags(registry, Map.of(
            "desktop", "Device",
            "firefox", "Browser",
            "skip", "Excluded",
            "meta", "Analytics and SEO",
            "WIP", "Work in Progress",
            "jumped", "Excluded",
            "hopped", "Excluded"
        ));
    }

    @Test
    void overrideApplicationLevelCategoryWithProjectLevelOnes() {
        GherkinOverviewProjectState.getInstance(getProject()).useProjectLevelMappings = true;
        GherkinOverviewProjectState.getInstance(getProject()).mappings = List.of(
            new CategoryAndTags("Ignored", "ignore"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());

        assertCategoriesOfTags(registry, Map.of(
            "desktop", "Device",
            "firefox", "Browser",
            "skip", "Excluded",
            "meta", "Analytics and SEO",
            "WIP", "Work in Progress",
            "ignore", "Ignored",
            "sanity", "Test Pack",
            "e2e", "Test Pack"
        ));
    }

    //putMappingsFrom

    @Test
    void putMappingsFromListOfCategoriesAndTags() {
        TagCategoryRegistry registry = new TagCategoryRegistry(getProject());
        var mappings = List.of(
            new CategoryAndTags("Excluded", "jumped,hopped"),
            new CategoryAndTags("Test Pack", "sanity, e2e"));

        registry.putMappingsFrom(mappings);

        assertCategoriesOfTags(registry, Map.of(
            "jumped", "Excluded",
            "hopped", "Excluded",
            "sanity", "Test Pack",
            "e2e", "Test Pack"
        ));
    }

    private void assertCategoriesOfTags(TagCategoryRegistry registry, Map<String, String> tagsToCategories) {
        assertSoftly(s ->
            tagsToCategories.forEach((tag, category) ->
                s.assertThat(registry.categoryOf(tag)).isEqualTo(category)));
    }

    //categoryOf

    @ParameterizedTest
    @MethodSource("tagsAndCategories")
    void testCategoryRetrievalForTags(String tag, String category) {
        var registry = new TagCategoryRegistry(getProject());

        assertThat(registry.categoryOf(tag)).isEqualTo(category);
    }

    public static Stream<Arguments> tagsAndCategories() {
        return Stream.of(
            argumentSet("returns category of existing tag", "desktop", "Device"),
            argumentSet("returns no category for not mapped tag", "non-mapped", null),
            argumentSet("returns category for regex based tag", "JIRA-1234", "Jira")
        );
    }
}
