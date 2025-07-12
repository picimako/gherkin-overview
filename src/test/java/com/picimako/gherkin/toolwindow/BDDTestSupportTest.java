//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BDDTestSupport}.
 */
final class BDDTestSupportTest extends GherkinOverviewTestBase {

    @Test
    void shouldReturnFirstGherkinTagForName() {
        PsiFile psiFile = configureByFile("the_gherkin.feature");

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@regression");

        assertThat(tag).isNotNull()
            .extracting(GherkinTag::getName).isEqualTo("@regression");
    }

    @Test
    void shouldReturnFirstMetaKeyForName() {
        PsiFile psiFile = configureByFile("Story.story");

        PsiElement metaKey = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@Disabled");

        assertThat(metaKey).isNotNull()
            .extracting(PsiElement::getText).isEqualTo("@Disabled");
    }

    @Test
    void shouldReturnNoGherkinTagIfFileDoesntContainTagForName() {
        PsiFile psiFile = configureByFile("the_gherkin.feature");

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@nonexistent");

        assertThat(tag).isNull();
    }

    @Test
    void shouldReturnNoMetaKeyIfFileDoesntContainMetaKeyForName() {
        PsiFile psiFile = configureByFile("Story.story");

        PsiElement tag = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@nonexistent");

        assertThat(tag).isNull();
    }
}
