//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static java.util.Comparator.comparing;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.intellij.openapi.project.Project;

import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;

import org.jetbrains.annotations.NotNull;

/**
 * Stores common properties of nodes.
 */
public abstract class AbstractNodeType implements NodeType {

    protected final Project project;
    protected String displayName;

    protected AbstractNodeType(@NotNull String displayName, @NotNull Project project) {
        this.displayName = displayName;
        this.project = project;
    }

    public String getDisplayName() {
        return displayName;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNodeType that = (AbstractNodeType) o;
        return Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName);
    }
}
