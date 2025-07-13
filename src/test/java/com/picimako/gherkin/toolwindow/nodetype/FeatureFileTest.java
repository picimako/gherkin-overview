//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;

import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link FeatureFile}.
 */
final class FeatureFileTest extends MediumBasePlatformTestCase {

    private FeatureFile featureFile;

    //FeatureFile()

    @Test
    void calculatesTagOccurrenceCountsUponInstantiation() {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        registry.init(1);

        assertThat(registry.getTagOccurrences()).isEmpty();

        setupTestObjects();

        assertThat(registry.getTagOccurrences()).containsOnlyKeys("/src/the_gherkin.feature");
        assertThat(registry.getTagOccurrences().get("/src/the_gherkin.feature")).isNotEmpty();
    }

    //hasFileName

    @Test
    void hasFileName() {
        setupTestObjects();

        assertThat(featureFile.hasFileName("the_gherkin.feature")).isTrue();
    }

    @Test
    void doesntHaveFileName() {
        setupTestObjects();

        assertThat(featureFile.hasFileName("not-matching.feature")).isFalse();
    }

    //setDisplayNameWithFeatureName

    @Test
    void setsDisplayNameWithFeatureName() {
        setupTestObjects();

        featureFile.setDisplayNameWithFeatureName("Smoke testing");
        assertThat(featureFile.displayName).isEqualTo("the_gherkin.feature [Smoke testing]");
    }

    //setDisplayNameWithPath

    @Test
    void setsDisplayNameWithProjectRootPath() {
        setupTestObjects();

        featureFile.setDisplayNameWithPath();

        assertThat(featureFile.displayName).isEqualTo("the_gherkin.feature [/]");
    }

    @Test
    void setsDisplayNameWithRelativePath() {
        setupTestObjects();

        VirtualFile evenmore = copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        FeatureFile nestedFeature = new FeatureFile(evenmore, "youtube", getProject());

        nestedFeature.setDisplayNameWithPath();

        assertThat(nestedFeature.displayName).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]");
    }

    //toString

    @ParameterizedTest
    @MethodSource("toStrings")
    void testToString(StatisticsType statisticsType, String toString) {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = statisticsType;
        setupTestObjects();

        assertThat(featureFile).hasToString(toString);
    }

    private static Stream<Arguments> toStrings() {
        return Stream.of(
            argumentSet("returns disabled toString()", StatisticsType.DISABLED, "the_gherkin.feature"),
            argumentSet("builds simplified toString()", StatisticsType.SIMPLIFIED, "the_gherkin.feature (2)"),
            argumentSet("builds detailed toString()", StatisticsType.DETAILED, "the_gherkin.feature - 2 occurrences")
        );
    }

    private void setupTestObjects() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        featureFile = new FeatureFile(theGherkin, "youtube", getProject());
    }
}
