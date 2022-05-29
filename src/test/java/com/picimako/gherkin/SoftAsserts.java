//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;

/**
 * Utility for soft assertions.
 */
public final class SoftAsserts {

    /**
     * Runs soft assertions on the provided assertions.
     */
    public static void assertSoftly(Consumer<SoftAssertions>... assertions) {
        SoftAssertions softly = new SoftAssertions();
        for (var assertion : assertions) {
            assertion.accept(softly);
        }
        softly.assertAll();
    }

    private SoftAsserts() {
        //Utility class
    }
}
