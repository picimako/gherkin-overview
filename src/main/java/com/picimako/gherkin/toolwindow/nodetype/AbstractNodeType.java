//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static java.util.Comparator.comparing;

import java.util.List;
import java.util.function.Supplier;

import com.intellij.openapi.project.Project;

import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Stores common properties of nodes.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = "project")
public abstract class AbstractNodeType implements NodeType {

    @Getter
    protected String displayName;
    protected final Project project;

    /**
     * Return a toString value based on what type of statistics should be displayed in the Gherkin Tags tool window.
     *
     * @param simplified the toString supplier for the Simplified statistics
     * @param detailed   the toString supplier for the Detailed statistics
     */
    public String getToString(Supplier<String> simplified, Supplier<String> detailed) {
        String toString = displayName;
        switch (GherkinTagsToolWindowSettings.getInstance(project).statisticsType) {
            case SIMPLIFIED:
                toString = simplified.get();
                break;
            case DETAILED:
                toString = detailed.get();
                break;
            default:
        }
        return toString;
    }

    /**
     * Returns true if this node has the given name, false otherwise.
     */
    public boolean hasName(String name) {
        return name != null && name.equals(displayName);
    }

    /**
     * Sorts the argument list of elements based on the elements' display names if it contains more than one element.
     *
     * @param elements the list of elements to sort
     */
    protected void sortIfContainsMultiple(List<? extends AbstractNodeType> elements) {
        if (elements.size() > 1) {
            elements.sort(comparing(element -> element.displayName.toLowerCase()));
        }
    }
}
