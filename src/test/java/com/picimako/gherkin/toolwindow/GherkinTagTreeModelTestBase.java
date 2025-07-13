//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

abstract class GherkinTagTreeModelTestBase extends MediumBasePlatformTestCase {

    protected abstract ModelDataRoot getRoot();

    protected void validateCategories(List<String> categories) {
        assertThat(getRoot().getModules())
            .flatMap(ContentRoot::getCategories)
            .extracting(Category::getDisplayName)
            .containsExactlyInAnyOrderElementsOf(categories);
    }

    protected void validateCategoryToTagOrMetaMappings(Map<String, List<String>> expectedCategoryTagOrMetaMappings, ModelDataRoot root) {
        assertSoftly(s ->
            expectedCategoryTagOrMetaMappings.forEach((category, tags) ->
                s.assertThat(root.getModules().getFirst().findCategory(category).get().getTags())
                    .extracting(Tag::getDisplayName)
                    .containsExactlyInAnyOrderElementsOf(tags)));
    }

    protected void validateTagToFileMappings(Map<String, List<VirtualFile>> expectedTagGherkinOrStoryFileMappings, ModelDataRoot root) {
        Map<String, Tag> tags = root.getContentRoots().getFirst().getCategories().stream()
            .flatMap(category -> category.getTags().stream())
            .collect(toMap(Tag::getDisplayName, Function.identity()));

        assertSoftly(s ->
            expectedTagGherkinOrStoryFileMappings.forEach((tag, gherkinFiles) -> {
                assertThat(tags).containsKey(tag);
                s.assertThat(tags.keySet()).contains(tag);
                s.assertThat(tags.get(tag).getGherkinFiles()).containsExactlyInAnyOrderElementsOf(gherkinFiles);
            }));
    }
}
