//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * A light test case that creates a new project descriptor for each test case, and by that making sure that a new
 * project is created for each test method.
 * <p>
 * This can help with resetting project services between tests, but not having the burden to use heavy tests.
 */
public abstract class MediumBasePlatformTestCase extends BasePlatformTestCase {

    /**
     * To re-create project for each test.
     * <p>
     * See last sentence at <a href="https://plugins.jetbrains.com/docs/intellij/light-and-heavy-tests.html#light-tests">Light tests</a>.
     */
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new LightProjectDescriptor();
    }
}
