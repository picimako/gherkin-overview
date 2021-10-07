/*
 * Copyright 2021 Tamás Balog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Unit test for {@link BDDTestSupport}.
 */
public class BDDTestSupportTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    public void testShouldReturnFirstGherkinTagForName() {
        PsiFile psiFile = myFixture.configureByFile("the_gherkin.feature");

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@regression");

        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo("@regression");
    }

    public void testShouldReturnFirstMetaKeyForName() {
        PsiFile psiFile = myFixture.configureByFile("Story.story");

        PsiElement metaKey = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@Disabled");

        assertThat(metaKey).isNotNull();
        assertThat(metaKey.getText()).isEqualTo("@Disabled");
    }

    public void testShouldReturnNoGherkinTagIfFileDoesntContainTagForName() {
        PsiFile psiFile = myFixture.configureByFile("the_gherkin.feature");

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@nonexistent");

        assertThat(tag).isNull();
    }

    public void testShouldReturnNoMetaKeyIfFileDoesntContainMetaKeyForName() {
        PsiFile psiFile = myFixture.configureByFile("Story.story");

        PsiElement tag = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@nonexistent");

        assertThat(tag).isNull();
    }
}
