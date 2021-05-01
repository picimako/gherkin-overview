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

package com.picimako.gherkin.settings;

import java.util.Objects;

/**
 * Stores a category to tags mapping.
 * <p>
 * The tags are stored as comma separated values.
 * <p>
 * {@link Cloneable} is implemented so that this type of objects are handled properly during the IDE Settings workflow.
 * <p>
 * e.g. com.intellij.lang.ant.config.impl.BuildFileProperty
 * e.g. com.intellij.execution.util.EnvironmentVariable
 */
public final class CategoryAndTags implements Cloneable {

    private String category;
    private String tags;

    public CategoryAndTags() {
        this("", "");
    }

    public CategoryAndTags(String category, String tags) {
        this.category = category;
        this.tags = tags;
    }

    public CategoryAndTags(CategoryAndTags cat) {
        this(cat.category, cat.tags);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    @Override
    public CategoryAndTags clone() {
        try {
            return (CategoryAndTags) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryAndTags that = (CategoryAndTags) o;
        return Objects.equals(category, that.category) && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, tags);
    }

    @Override
    public String toString() {
        return "CategoryAndTag{" +
            "category='" + category + '\'' +
            ", tags='" + tags + '\'' +
            '}';
    }
}
