//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;

/**
 * Unit test for {@link Tag}.
 */
public class TagTest extends BasePlatformTestCase {

    private VirtualFile theGherkin;
    private VirtualFile aGherkin;
    private VirtualFile forStatistics;
    private Tag tag;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
    }

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //hasFeatureFile

    public void testHasFeatureFile() {
        setupTestObjects();

        assertThat(tag.hasFeatureFile()).isTrue();
    }

    public void testDoesntHaveFeatureFile() {
        setupTestObjects();
        tag.remove(theGherkin);
        tag.remove(aGherkin);
        tag.remove(forStatistics);

        assertThat(tag.hasFeatureFile()).isFalse();
    }

    //contains

    public void testContainsVirtualFile() {
        setupTestObjects();

        assertThat(tag.contains(theGherkin)).isTrue();
    }

    public void testDoesntContainVirtualFile() {
        setupTestObjects();
        tag.remove(theGherkin);

        assertThat(tag.contains(theGherkin)).isFalse();
    }

    //add

    public void testShouldAddGherkinFile() {
        setupTestObjects();

        assertThat(tag.getGherkinFiles()).containsExactly(forStatistics, theGherkin, aGherkin);
    }

    public void testShouldNotAddGherkinFile() {
        setupTestObjects();
        Tag tag = new Tag("smoke", theGherkin, getProject());
        tag.add(theGherkin);

        assertThat(tag.getGherkinFiles()).containsOnly(theGherkin);
    }

    public void testAddsFeatureFileWithRelativePathInNameAndUpdatesPreviouslyAddedFeatureFileNames() {
        VirtualFile nested = myFixture.copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = myFixture.copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        VirtualFile evenMoreMore = myFixture.copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature");

        tag.add(evenMore);

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]")
        );

        tag.add(evenMoreMore);

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]"),
            softly -> softly.assertThat(featureFiles.get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]")
        );
    }

    //updateDisplayNames

    public void testUpdatesDisplayNameWithPathForMoreThanTwoFeatureFilesInATagWithTheSameName() {
        PsiFile nested = myFixture.configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmoremore = myFixture.configureByFile("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject()).add(evenmoremore.getVirtualFile());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]")
        );

        GherkinFeature feature = GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Same name");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> ((GherkinFile) evenmoremore).getFeatures()[0].replace(feature), "Replace", "group.id"));

        tag.updateDisplayNames(evenmoremore.getVirtualFile());

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]")
        );
    }

    public void testUpdatesDisplayNameWithPathForMoreThanTwoStoryFilesInATagWithTheSameName() {
        PsiFile nested = myFixture.configureByFile("nested/story_with_same_name.story");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("story_with_same_name.story");

        PsiFile evenmoremore = myFixture.configureByFile("nested/evenmore/evenmoremore/story_with_same_name.story");
        tag.add(evenmoremore.getVirtualFile());

        tag.updateDisplayNames(evenmoremore.getVirtualFile());

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("story_with_same_name.story [nested]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("story_with_same_name.story [nested/evenmore/evenmoremore]")
        );
    }

    public void testUpdatesDisplayNameWithFeatureNameForMoreThanTwoFeatureFilesInATagWithTheSameName() {
        PsiFile nested = myFixture.configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmore = myFixture.configureByFile("nested/evenmore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested.getVirtualFile(), getProject()).add(evenmore.getVirtualFile());

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore]")
        );

        GherkinFeature feature = GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Not same name");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> ((GherkinFile) nested).getFeatures()[0].replace(feature), "Replace", "group.id"));

        tag.updateDisplayNames(nested.getVirtualFile());

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Not same name]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]")
        );
    }

    //remove

    public void testShouldRemoveGherkinFile() {
        setupTestObjects();

        tag.remove(theGherkin);

        assertThat(tag.getFeatureFiles()).extracting(FeatureFile::getFile).containsExactly(forStatistics, aGherkin);
    }

    public void testUponRemovalWithOneRemainingFileRestoresDisplayNameToFileName() {
        VirtualFile nested = myFixture.copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = myFixture.copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(evenMore);

        assertThat(tag.getFeatureFiles().get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]");

        tag.remove(nested);

        assertThat(tag.getFeatureFiles().get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature");
    }

    public void testUponRemovalWithOneFileRemainingWithTheSameNameRestoresDisplayNameToFileName() {
        VirtualFile aGherkin = myFixture.copyFileToProject("A_gherkin.feature");
        VirtualFile nested = myFixture.copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenmoremore = myFixture.copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(aGherkin).add(evenmoremore);

        assertSoftly(
            softly -> softly.assertThat(tag.getFeatureFiles().get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]"),
            softly -> softly.assertThat(tag.getFeatureFiles().get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]")
        );

        tag.remove(nested);

        assertThat(tag.getFeatureFiles().get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature");
    }

    public void testUponRemovalWithMultipleFilesRemainingWithTheSameNameUpdatesDisplayName() {
        VirtualFile nested = myFixture.copyFileToProject("nested/gherkin_with_same_name.feature");
        VirtualFile evenMore = myFixture.copyFileToProject("nested/evenmore/gherkin_with_same_name.feature");
        VirtualFile evenmoremore = myFixture.copyFileToProject("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        Tag tag = new Tag("smoke", nested, getProject()).add(evenMore).add(evenmoremore);

        List<FeatureFile> featureFiles = tag.getFeatureFiles();
        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested]"),
            softly -> softly.assertThat(featureFiles.get(2).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]")
        );

        tag.remove(nested);

        assertSoftly(
            softly -> softly.assertThat(featureFiles.get(0).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Same name]"),
            softly -> softly.assertThat(featureFiles.get(1).getDisplayName()).isEqualTo("gherkin_with_same_name.feature [Almost same name]")
        );
    }

    //sort

    public void testShouldSortGherkinFiles() {
        setupTestObjects();

        tag.sort();

        assertThat(tag.getGherkinFiles()).containsExactly(aGherkin, forStatistics, theGherkin);
    }

    //toString

    public void testShouldShowSimplifiedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        setupTestObjects();

        assertThat(tag).hasToString("youtube (5)");
    }

    public void testShouldShowDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;
        setupTestObjects();

        assertThat(tag).hasToString("youtube - 5 in 3 files");
    }

    public void testShouldNotShowStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;
        setupTestObjects();

        assertThat(tag).hasToString("youtube");
    }

    private void setupTestObjects() {
        theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();
        forStatistics = myFixture.configureByFile("for_statistics.feature").getVirtualFile();
        tag = new Tag("youtube", forStatistics, getProject());
        tag.add(theGherkin).add(aGherkin);
    }
}
