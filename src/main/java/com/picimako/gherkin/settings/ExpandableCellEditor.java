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
