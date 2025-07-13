//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.List;
import java.util.stream.Stream;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link Tag}.
 */
final class TagTest extends GherkinOverviewTestBase {
    private VirtualFile theGherkin;
    private VirtualFile aGherkin;
    private VirtualFile forStatistics;
    private Tag tag;

    @BeforeEach
    void setUp() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
    }

    //hasFeatureFile

    @Test
    void hasFeatureFile() {
        setupTestObjects();

        assertThat(tag.hasFeatureFile()).isTrue();
    }

    @Test
    void doesntHaveFeatureFile() {
        setupTestObjects();
        tag.remove(theGherkin);
        tag.remove(aGherkin);
        tag.remove(forStatistics);

        assertThat(tag.hasFeatureFile()).isFalse();
    }

    //contains

    @Test
    void containsVirtualFile() {
        setupTestObjects();

        assertThat(tag.contains(theGherkin)).isTrue();
    }

    @Test
    void doesntContainVirtualFile() {
        setupTestObjects();
        tag.remove(theGherkin);

        assertThat(tag.contains(theGherkin)).isFalse();
    }

    //add

    @Test
    void shouldAddGherkinFile() {
        setupTestObjects();

        assertThat(tag.getGherkinFiles()).containsExactly(forStatistics, theGherkin, aGherkin);
    }

    @Test
    void shouldNotAddGherkinFile() {
        setupTestObjects();
        Tag tag = new Tag("smoke", theGherkin, getProject());
        tag.add(theGherkin);

        assertThat(tag.getGherkinFiles()).containsOnly(theGherkin);
    }

    @Test
    void addsFeatureFileWithRelativePathInNameAndUpdatesPreviouslyAddedFeatureFileNames() {
        VirtualFile nested = copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        VirtualFile evenMoreMore = copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature");

        tag.add(evenMore);

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]");
        });

        tag.add(evenMoreMore);

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]");
            s.assertThat(featureFiles.get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]");
        });
    }

    //updateDisplayNames

    @Test
    void updatesDisplayNameWithPathForMoreThanTwoFeatureFilesInATagWithTheSameName() {
        PsiFile nested = configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmoremore = configureByFile("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject()).add(evenmoremore.getVirtualFile());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]");
        });

        GherkinFeature feature = ReadAction.compute(() -> GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Same name"));
        executeCommandProcessorCommand(() -> ((GherkinFile) evenmoremore).getFeatures()[0].replace(feature), "Replace", "group.id");

        tag.updateDisplayNames(evenmoremore.getVirtualFile());

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]");
        });
    }

    @Test
    void updatesDisplayNameWithPathForMoreThanTwoStoryFilesInATagWithTheSameName() {
        PsiFile nested = configureByFile("nested/story_with_same_name.story");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("story_with_same_name.story");

        PsiFile evenmoremore = configureByFile("nested/evenmore/evenmoremore/story_with_same_name.story");
        tag.add(evenmoremore.getVirtualFile());

        tag.updateDisplayNames(evenmoremore.getVirtualFile());

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("story_with_same_name.story [nested]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("story_with_same_name.story [nested/evenmore/evenmoremore]");
        });
    }

    @Test
    void updatesDisplayNameWithFeatureNameForMoreThanTwoFeatureFilesInATagWithTheSameName() {
        PsiFile nested = configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmore = configureByFile("nested/evenmore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject()).add(evenmore.getVirtualFile());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]");
        });

        GherkinFeature feature = ReadAction.compute(() -> GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Not same name"));
        executeCommandProcessorCommand(() -> ((GherkinFile) nested).getFeatures()[0].replace(feature), "Replace", "group.id");

        tag.updateDisplayNames(nested.getVirtualFile());

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Not same name]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]");
        });
    }

    //remove

    @Test
    void shouldRemoveGherkinFile() {
        setupTestObjects();

        tag.remove(theGherkin);

        assertThat(tag.getFeatureFiles()).extracting(FeatureFile::getFile).containsExactly(forStatistics, aGherkin);
    }

    @Test
    void uponRemovalWithOneRemainingFileRestoresDisplayNameToFileName() {
        VirtualFile nested = copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(evenMore);

        assertThat(tag.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");

        tag.remove(nested);

        assertThat(tag.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature");
    }

    @Test
    void uponRemovalWithOneFileRemainingWithTheSameNameRestoresDisplayNameToFileName() {
        VirtualFile aGherkin = copyFileToProject("A_gherkin.feature");
        VirtualFile nested = copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenmoremore = copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(aGherkin).add(evenmoremore);

        assertSoftly(s -> {
            s.assertThat(tag.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]");
            s.assertThat(tag.getFeatureFiles().get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]");
        });

        tag.remove(nested);

        assertThat(tag.getFeatureFiles().get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature");
    }

    @Test
    void uponRemovalWithMultipleFilesRemainingWithTheSameNameUpdatesDisplayName() {
        VirtualFile nested = copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        VirtualFile evenmoremore = copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(evenMore).add(evenmoremore);

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");
            s.assertThat(featureFiles.get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]");
        });

        tag.remove(nested);

        assertSoftly(s -> {
            s.assertThat(featureFiles.getFirst().getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]");
            s.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]");
        });
    }

    //sort

    @Test
    void shouldSortGherkinFiles() {
        setupTestObjects();

        tag.sort();

        assertThat(tag.getGherkinFiles()).containsExactly(aGherkin, forStatistics, theGherkin);
    }

    //toString

    @ParameterizedTest
    @MethodSource("toStrings")
    void testToString(StatisticsType statisticsType, String toString) {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = statisticsType;
        setupTestObjects();

        assertThat(tag).hasToString(toString);
    }

    private static Stream<Arguments> toStrings() {
        return Stream.of(
            argumentSet("returns disabled toString()", StatisticsType.DISABLED, "youtube"),
            argumentSet("builds simplified toString()", StatisticsType.SIMPLIFIED, "youtube (5)"),
            argumentSet("builds detailed toString()", StatisticsType.DETAILED, "youtube - 5 in 3 files")
        );
    }

    private void setupTestObjects() {
        theGherkin = configureVirtualFile("the_gherkin.feature");
        aGherkin = configureVirtualFile("A_gherkin.feature");
        forStatistics = configureVirtualFile("for_statistics.feature");
        tag = new Tag("youtube", forStatistics, getProject());
        tag.add(theGherkin).add(aGherkin);
    }
}
