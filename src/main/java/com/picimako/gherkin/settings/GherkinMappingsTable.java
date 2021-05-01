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

import java.awt.*;
import javax.swing.table.TableCellEditor;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.execution.util.StringWithNewLinesCellEditor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.picimako.gherkin.resources.GherkinBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom {@link ListTableWithButtons} implementation to provide a UI for Gherkin Category to Tags mappings.
 * <p>
 * Moving tables row up and down is not yet supported by this implementation.
 */
public class GherkinMappingsTable extends ListTableWithButtons<CategoryAndTags> {

    /**
     * Height of the table is increased to provide more visibility of the inner elements.
     */
    public GherkinMappingsTable() {
        getComponent().setPreferredSize(new Dimension(getComponent().getWidth(), 250));
    }

    @Override
    protected ListTableModel<CategoryAndTags> createListModel() {
        final ColumnInfo<CategoryAndTags, String> categoryColumn = new BaseCategoryTagsColumnInfo(GherkinBundle.settings("table.column.category")) {
            @Nullable
            @Override
            public String valueOf(CategoryAndTags categoryAndTag) {
                return categoryAndTag.getCategory();
            }

            @Override
            public void setValue(CategoryAndTags categoryAndTag, @NlsContexts.ListItem String value) {
                if (!value.equals(valueOf(categoryAndTag))) {
                    categoryAndTag.setCategory(value);
                    setModified();
                }
            }
        };

        final ColumnInfo<CategoryAndTags, String> tagsColumn = new BaseCategoryTagsColumnInfo(GherkinBundle.settings("table.column.tags")) {
            @Nullable
            @Override
            public String valueOf(CategoryAndTags categoryAndTag) {
                return categoryAndTag.getTags();
            }

            @Override
            public void setValue(CategoryAndTags categoryAndTag, @NlsContexts.ListItem String value) {
                if (!value.equals(valueOf(categoryAndTag))) {
                    categoryAndTag.setTags(value);
                    setModified();
                }
            }

            @NotNull
            @Override
            public TableCellEditor getEditor(CategoryAndTags variable) {
                return new ExpandableCellEditor();
            }
        };

        return new ListTableModel<>(categoryColumn, tagsColumn);
    }

    @Override
    protected CategoryAndTags createElement() {
        return new CategoryAndTags();
    }

    @Override
    protected boolean isEmpty(CategoryAndTags element) {
        return StringUtil.isEmpty(element.getCategory()) && StringUtil.isEmpty(element.getTags());
    }

    @Override
    protected CategoryAndTags cloneElement(CategoryAndTags variable) {
        return variable.clone();
    }

    @Override
    protected boolean canDeleteElement(CategoryAndTags selection) {
        return true;
    }

    /**
     * A base {@link ColumnInfo} implementation for Category-Tag mapping tables.
     */
    private static abstract class BaseCategoryTagsColumnInfo extends ElementsColumnInfoBase<CategoryAndTags> {
        protected BaseCategoryTagsColumnInfo(@NlsContexts.ColumnName String name) {
            super(name);
        }

        @Override
        protected @Nullable @NlsContexts.Tooltip String getDescription(CategoryAndTags element) {
            return null;
        }

        @NotNull
        @Override
        public TableCellEditor getEditor(CategoryAndTags variable) {
            return new StringWithNewLinesCellEditor();
        }

        @Override
        public boolean isCellEditable(CategoryAndTags categoryAndTags) {
            return true;
        }
    }
}
