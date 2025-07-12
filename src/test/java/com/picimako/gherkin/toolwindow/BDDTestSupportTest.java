//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Unit test for {@link BDDTestSupport}.
 */
public class BDDTestSupportTest extends GherkinOverviewTestBase {

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
