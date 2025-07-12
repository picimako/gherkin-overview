//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;

import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link AbstractNodeType}.
 */
final class AbstractNodeTypeTest extends GherkinOverviewTestBase {

    //getToString

    @ParameterizedTest
    @MethodSource("toStrings")
    void testToString(StatisticsType statisticsType, String toString) {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = statisticsType;

        assertThat(getToString()).isEqualTo(toString);
    }

    private static Stream<Arguments> toStrings() {
        return Stream.of(
            argumentSet("returns disabled toString()", StatisticsType.DISABLED, "Node name"),
            argumentSet("builds simplified toString()", StatisticsType.SIMPLIFIED, "simplified"),
            argumentSet("builds detailed toString()", StatisticsType.DETAILED, "detailed")
        );
    }

    private String getToString() {
        return new AbstractNodeType("Node name", getProject()) {
        }.getToString(() -> "simplified", () -> "detailed");
    }
}
