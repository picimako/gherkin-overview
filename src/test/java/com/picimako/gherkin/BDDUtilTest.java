//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Unit test for {@link BDDUtil}.
 */
public class BDDUtilTest extends BasePlatformTestCase {

    //isABDDFile(PsiFile)

    public void testIsABDDFileGherkin() {
        var gherkinFile = myFixture.configureByText("gherkin.feature", "");

        assertThat(BDDUtil.isABDDFile(gherkinFile)).isTrue();
    }

    public void testIsABDDFileStory() {
        var storyFile = myFixture.configureByText("story.story", "");

        assertThat(BDDUtil.isABDDFile(storyFile)).isTrue();
    }

    public void testIsNotABDDFile() {
        var javaFile = myFixture.configureByText("SomeClass.java", "");

        assertThat(BDDUtil.isABDDFile(javaFile)).isFalse();
    }

    //isABDDFile(VirtualFile)

    public void testIsABDDVirtualFileGherkin() {
        var gherkinFile = myFixture.configureByText("gherkin.feature", "");

        assertThat(BDDUtil.isABDDFile(gherkinFile.getVirtualFile(), getProject())).isTrue();
    }

    public void testIsABDDVirtualFileStory() {
        var storyFile = myFixture.configureByText("story.story", "");

        assertThat(BDDUtil.isABDDFile(storyFile.getVirtualFile(), getProject())).isTrue();
    }

    public void testIsNotABDDVirtualFile() {
        var javaFile = myFixture.configureByText("SomeClass.java", "");

        assertThat(BDDUtil.isABDDFile(javaFile.getVirtualFile(), getProject())).isFalse();
    }
}
