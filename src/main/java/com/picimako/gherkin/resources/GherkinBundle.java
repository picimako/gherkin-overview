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

package com.picimako.gherkin.resources;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Resource bundle for all messages in this plugin.
 *
 * @since 0.1.0
 */
public class GherkinBundle extends DynamicBundle {

    @NonNls
    private static final String GHERKIN_BUNDLE = "messages.GherkinBundle";
    private static final GherkinBundle INSTANCE = new GherkinBundle();

    private GherkinBundle() {
        super(GHERKIN_BUNDLE);
    }

    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = GHERKIN_BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    /**
     * Retrieves a Gherkin Tags tool window specific message for the provided id.
     *
     * @param id the suffix of the message key
     * @return the actual message
     */
    public static String toolWindow(@NonNls String id, Object @NotNull ... params) {
        return message("gherkin.overview.toolwindow." + id, params);
    }

    /**
     * Retrieves a plugin settings specific message for the provided id.
     *
     * @param id the suffix of the message key
     * @return the actual message
     */
    public static String settings(@NonNls String id) {
        return message("gherkin.overview.settings." + id);
    }
}
