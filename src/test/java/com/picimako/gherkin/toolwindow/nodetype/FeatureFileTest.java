//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;

/**
 * Unit test for {@link FeatureFile}.
 */
public class FeatureFileTest extends MediumBasePlatformTestCase {

    private FeatureFile featureFile;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //FeatureFile()

    public void testCalculatesTagOccurrenceCountsUponInstantiation() {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        registry.init(1);

        assertThat(registry.getTagOccurrences()).isEmpty();

        setupTestObjects();

        assertThat(registry.getTagOccurrences()).containsOnlyKeys("/src/the_gherkin.feature");
        assertThat(registry.getTagOccurrences().get("/src/the_gherkin.feature")).isNotEmpty();
    }

    //hasFileName

    public void testHasFileName() {
        setupTestObjects();

        assertThat(featureFile.hasFileName("the_gherkin.feature")).isTrue();
    }

    public void testDoesntHaveFileName() {
        setupTestObjects();

        assertThat(featureFile.hasFileName("not-matching.feature")).isFalse();
    }

    //setDisplayNameWithFeatureName

    public void testSetsDisplayNameWithFeatureName() {
        setupTestObjects();

        featureFile.setDisplayNameWithFeatureName("Smoke testing");
        assertThat(featureFile.displayName).isEqualTo("the_gherkin.feature [Smoke testing]");
    }

    //setDisplayNameWithPath

    public void testSetsDisplayNameWithProjectRootPath() {
        setupTestObjects();

        featureFile.setDisplayNameWithPath();

        assertThat(featureFile.displayName).isEqualTo("the_gherkin.feature [/]");
    }

    public void testSetsDisplayNameWithRelativePath() {
        setupTestObjects();

        VirtualFile evenmore = myFixture.copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        FeatureFile nestedFeature = new FeatureFile(evenmore, "youtube", getProject());

        nestedFeature.setDisplayNameWithPath();

        assertThat(nestedFeature.displayName).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]");
    }

    //toString

    public void testShouldShowSimplifiedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        setupTestObjects();

        assertThat(featureFile).hasToString("the_gherkin.feature (2)");
    }

    public void testShouldShowDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;
        setupTestObjects();

        assertThat(featureFile).hasToString("the_gherkin.feature - 2 occurrences");
    }

    public void testShouldNotShowStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;
        setupTestObjects();

        assertThat(featureFile).hasToString("the_gherkin.feature");
    }

    private void setupTestObjects() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        featureFile = new FeatureFile(theGherkin, "youtube", getProject());
    }
}
