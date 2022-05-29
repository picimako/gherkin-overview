//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.testFramework.LightPlatform4TestCase;

import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import org.junit.Test;

/**
 * Unit test for {@link AbstractNodeType}.
 */
public class AbstractNodeTypeTest extends LightPlatform4TestCase {

    //getToString

    @Test
    public void shouldReturnDisabledToString() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;

        assertThat(getToString()).isEqualTo("Node name");
    }

    @Test
    public void shouldBuildSimplifiedToString() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        assertThat(getToString()).isEqualTo("simplified");
    }

    @Test
    public void shouldBuildDetailedToString() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;

        assertThat(getToString()).isEqualTo("detailed");
    }

    private String getToString() {
        return new AbstractNodeType("Node name", getProject()) {
        }.getToString(() -> "simplified", () -> "detailed");
    }
}
