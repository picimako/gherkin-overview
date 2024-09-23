//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
     * Retrieves a plugin settings specific message for the provided id.
     *
     * @param id the suffix of the message key
     * @return the actual message
     */
    public static String settings(@NonNls String id) {
        return message("gherkin.overview.settings." + id);
    }
}
