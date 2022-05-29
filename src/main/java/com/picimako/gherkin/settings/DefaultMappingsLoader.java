//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads the default application-level category-tag mappings.
 */
public final class DefaultMappingsLoader {

    /**
     * Loads the default application-level category-tag mappings.
     *
     * @throws RuntimeException if the mapping could not be loaded
     */
    public static List<CategoryAndTags> loadDefaultApplicationLevelMappings() {
        try (InputStream mappingsResource = GherkinOverviewComponent.class.getResourceAsStream("/mapping/default_app_level_mappings.properties")) {
            final var categoriesAndTags = new ArrayList<CategoryAndTags>();
            var defaultAppLevelMappings = new Properties();
            defaultAppLevelMappings.load(mappingsResource);
            defaultAppLevelMappings.forEach((key, value) -> categoriesAndTags.add(new CategoryAndTags(key.toString(), value.toString())));
            return categoriesAndTags;
        } catch (IOException e) {
            throw new RuntimeException("Could not load default category-tag mappings.", e);
        }
    }

    private DefaultMappingsLoader() {
        //Utility class
    }
}
