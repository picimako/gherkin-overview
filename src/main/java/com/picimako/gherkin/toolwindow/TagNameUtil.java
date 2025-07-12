//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.JBehaveStoryService;

/**
 * Utilities for retrieving Gherkin tag and Story meta names.
 */
public final class TagNameUtil {

    /**
     * Returns the argument Gherkin tag's name without the leading @ symbol.
     */
    @NotNull
    public static String tagNameFrom(GherkinTag tag) {
        return tag.getName().substring(1);
    }

    /**
     * If both the Meta key and text are present, their texts are joined by a colon, otherwise returns the key's text
     * without the @ symbol.
     * <p>
     * {@code @Suite smoke regression} becomes {@code Suite:smoke} and {@code Suite:regression},
     * while {@code @E2E} becomes simply {@code E2E}.
     *
     * @param metaTextElement the meta text
     * @return the meta string to store as tag
     */
    @NotNull
    public static String metaNameFrom(@NotNull PsiElement metaKeyElement, @Nullable Collection<PsiElement> metaTextElement) {
        return metaTextElement != null && !metaTextElement.isEmpty()
            ? metaKeyElement.getText().substring(1) + ":" + metaTextElement.stream().map(PsiElement::getText).collect(joining(" "))
            : metaKeyElement.getText().substring(1);
    }

    /**
     * Gets the tag or meta name from the argument PSI element if the element is either a {@link GherkinTag}
     * or a {@link com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType#META_KEY}.
     *
     * @param element the element to inspect for the tag name
     * @return the tag or meta name, or null if the element is not eligible to get that information
     */
    @Nullable
    public static String determineTagOrMetaName(PsiElement element) {
        String tagName = null;
        if (element instanceof GherkinTag) {
            tagName = tagNameFrom((GherkinTag) element);
        } else {
            var storyService = element.getProject().getService(JBehaveStoryService.class);
            if (storyService.isMetaKey(element)) {
                tagName = metaNameFrom(element, storyService.collectMetaTextsForMetaKeyAsList(element));
            }
        }
        return tagName;
    }

    private TagNameUtil() {
        //Utility class
    }
}
