//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.table.TableCellEditor;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.execution.util.StringWithNewLinesCellEditor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.picimako.gherkin.resources.GherkinBundle;

/**
 * A custom {@link ListTableWithButtons} implementation to provide a UI for Gherkin Category to Tags mappings.
 * <p>
 * Moving tables row up and down is not yet supported by this implementation.
 */
final class GherkinMappingsTable extends ListTableWithButtons<CategoryAndTags> {

    /**
     * Actions in addition to the default Add and Remove.
     */
    private ExtraActionsSupplier extraActions = ExtraActionsSupplier.NO_ACTION;

    GherkinMappingsTable withExtraActions(ExtraActionsSupplier extraActions) {
        this.extraActions = extraActions;
        return this;
    }

    /**
     * Height of the table is increased to provide more visibility of the inner elements.
     * <p>
     * Due to the {@code getComponent()} call that set the extra actions under the hood,
     * this method must be called after {@code withExtraActions},and must be called whenever this table is instantiated.
     */
    GherkinMappingsTable init() {
        getComponent().setPreferredSize(new Dimension(getComponent().getWidth(), 250));
        return this;
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

    @Override
    protected AnActionButton @NotNull [] createExtraToolbarActions() {
        return extraActions.get();
    }

    /**
     * A base {@link ColumnInfo} implementation for Category-Tag mapping tables.
     */
    private abstract static class BaseCategoryTagsColumnInfo extends ElementsColumnInfoBase<CategoryAndTags> {
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

    /**
     * Provides extra action buttons for the {@link GherkinMappingsTable}.
     */
    @FunctionalInterface
    interface ExtraActionsSupplier extends Supplier<AnActionButton[]> {
        AnActionButton[] EMPTY = new AnActionButton[0];
        ExtraActionsSupplier NO_ACTION = () -> EMPTY;
    }

}
