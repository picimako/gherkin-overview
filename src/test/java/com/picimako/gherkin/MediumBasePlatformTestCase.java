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
