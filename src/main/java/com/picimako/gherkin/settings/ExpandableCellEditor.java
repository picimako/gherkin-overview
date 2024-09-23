//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import java.util.List;
import javax.swing.*;
import javax.swing.text.Document;

import com.intellij.ui.components.fields.ExpandableTextField;

/**
 * A cell editor implementation to provide an {@link ExpandableTextField}.
 * <p>
 * This is being used for the Tags column's cells to make it easier to edit values that would not fit the cell's
 * original width.
 * <p>
 * Individual tag values are split and joined by comma (,) characters upon expanding and collapsing the editor
 * respectively.
 */
final class ExpandableCellEditor extends DefaultCellEditor {
    ExpandableCellEditor() {
        super(new ExpandableTextField(s -> List.of(s.split(",")), strings -> String.join(",", strings)) {
            @Override
            public void setDocument(Document doc) {
                super.setDocument(doc);
                doc.putProperty("filterNewlines", Boolean.FALSE);
            }
        });
    }
}
