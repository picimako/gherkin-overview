//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import lombok.*;

/**
 * Stores a category to tags mapping.
 * <p>
 * The tags are stored as comma separated values.
 * <p>
 * {@link Cloneable} is implemented so that these type of objects are handled properly during the IDE Settings workflow.
 * <p>
 * e.g. com.intellij.lang.ant.config.impl.BuildFileProperty
 * e.g. com.intellij.execution.util.EnvironmentVariable
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class CategoryAndTags implements Cloneable {

    private String category;
    private String tags;

    public CategoryAndTags() {
        this("", "");
    }

    public CategoryAndTags(CategoryAndTags cat) {
        this(cat.category, cat.tags);
    }

    @Override
    public CategoryAndTags clone() {
        try {
            return (CategoryAndTags) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
