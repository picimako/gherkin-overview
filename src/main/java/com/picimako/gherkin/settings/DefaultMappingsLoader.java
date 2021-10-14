/*
 *  Copyright 2021 Tamás Balog
 *
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
