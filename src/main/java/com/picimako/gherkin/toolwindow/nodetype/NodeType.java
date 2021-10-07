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

package com.picimako.gherkin.toolwindow.nodetype;

import com.intellij.openapi.Disposable;

/**
 * Node type for the elements of the Gherkin tree.
 */
public interface NodeType extends Sortable, Disposable {

    /**
     * Returns the argument object as a {@link ContentRoot}.
     */
    static ContentRoot asContentRoot(Object node) {
        return (ContentRoot) node;
    }

    /**
     * Returns the argument object as a {@link Category}.
     */
    static Category asCategory(Object node) {
        return (Category) node;
    }

    /**
     * Returns the argument object as a {@link Tag}.
     */
    static Tag asTag(Object node) {
        return (Tag) node;
    }

    @Override
    default void dispose() {
    }
}
