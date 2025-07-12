//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.wm.ToolWindow;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;

/**
 * Unit test for {@link GherkinTagToolWindowUtil}.
 */
public class GherkinTagToolWindowUtilTest extends MediumBasePlatformTestCase {

    //getGherkinTagsToolWindow

    public void testReturnsGherkinTagToolWindow() {
        ToolWindowTestSupport.registerToolWindow(getProject());

        assertThat(GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject())).isNotNull();
    }

    public void testDoesntReturnGherkinTagToolWindowIfNotRegistered() {
        assertThat(GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject())).isNull();
    }

    //getToolWindowHider

    public void testReturnsToolWindowHider() {
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        ToolWindow gherkinTagsToolWindow = GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject());

        assertThat(GherkinTagToolWindowUtil.getToolWindowHider(gherkinTagsToolWindow).getComponent(0)).isInstanceOf(GherkinTagOverviewPanel.class);
    }
}