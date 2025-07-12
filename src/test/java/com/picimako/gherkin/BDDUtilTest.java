//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit test for {@link BDDUtil}.
 */
final class BDDUtilTest extends GherkinOverviewTestBase {

    @ParameterizedTest
    @CsvSource(value = {
        "gherkin.feature, true",
        "story.story, true",
        "SomeClass.java, false"
    })
    void testIsBddPsiFile(String fileName, boolean shouldBeABddFile) {
        var file = configureEmptyFile(fileName);

        assertThat(BDDUtil.isABDDFile(file)).isEqualTo(shouldBeABddFile);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "gherkin.feature, true",
        "story.story, true",
        "SomeClass.java, false"
    })
    void testIsBddVirtualFile(String fileName, boolean shouldBeABddFile) {
        var file = configureEmptyFile(fileName);

        assertThat(BDDUtil.isABDDFile(file.getVirtualFile(), getProject())).isEqualTo(shouldBeABddFile);
    }
}
