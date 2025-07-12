//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Base test class for all plugin tests.
 */
public abstract class GherkinOverviewTestBase extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }
}
