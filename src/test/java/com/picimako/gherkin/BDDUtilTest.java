/*
 *  Copyright 2021 Tamás Balog
 *
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Unit test for {@link BDDUtil}.
 */
public class BDDUtilTest extends BasePlatformTestCase {

    //isABDDFile

    public void testIsABDDFileGherkin() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature", "");

        assertThat(BDDUtil.isABDDFile(gherkinFile)).isTrue();
    }

    public void testIsABDDFileStory() {
        PsiFile storyFile = myFixture.configureByText("story.story", "");

        assertThat(BDDUtil.isABDDFile(storyFile)).isTrue();
    }

    public void testIsNotABDDFile() {
        PsiFile storyFile = myFixture.configureByText("SomeClass.java", "");

        assertThat(BDDUtil.isABDDFile(storyFile)).isFalse();
    }

}