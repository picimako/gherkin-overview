//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asCategory;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asTag;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.toolwindow.nodetype.CategoriesHolder;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * Model object for displaying the structure of the underlying model data without grouping them by any type of
 * content root.
 * <p>
 * This is a four-level model consisting of the following levels:
 * <pre>{@code
 * - Gherkin Tags               <-- This is the root node. Permanent, can't be removed.
 *      - Category              <-- The category a Gherkin tag is associated to for grouping.
 *          - Tag               <-- The Gherkin tag.
 *              - Gherkin file  <-- One or more 'FeatureFile's.
 * }</pre>
 */
public final class ProjectSpecificGherkinTagTreeModel extends GherkinTagTreeModel {

    public ProjectSpecificGherkinTagTreeModel(Project project) {
        super(project);
    }

    public ProjectSpecificGherkinTagTreeModel(ModelDataRoot data, Project project) {
        super(data, project);
    }

    @Override
    protected CategoriesHolder getContentRoot(PsiFile file) {
        return data;
    }

    // The methods below are responsible for building the actual tree model from the backing model data.

    @Override
    public Object getChild(Object parent, int index) {
        Object child = null;
        switch (parent) {
            case ModelDataRoot __ -> child = data.getCategories().get(index);
            case Category __ -> child = asCategory(parent).getTags().get(index);
            case Tag __ -> child = asTag(parent).getFeatureFiles().get(index);
            case null, default -> {
            }
        }
        return child;
    }

    @Override
    public int getChildCount(Object parent) {
        int count = 0;
        switch (parent) {
            case ModelDataRoot __ -> count = data.getCategories().size();
            case Category __ -> count = asCategory(parent).getTags().size();
            case Tag __ -> count = asTag(parent).getFeatureFiles().size();
            case null, default -> {
            }
        }
        return count;
    }

    @Override
    public boolean isLeaf(Object node) {
        boolean isLeaf = data.getCategories().isEmpty();
        switch (node) {
            case Category __ -> isLeaf = !asCategory(node).hasTag();
            case Tag __ -> isLeaf = !asTag(node).hasFeatureFile();
            case FeatureFile __ -> isLeaf = true;
            case null, default -> {
            }
        }
        return isLeaf;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int indexOfChild = 0;
        if (parent != null && child != null) {
            switch (parent) {
                case ModelDataRoot __ -> indexOfChild = data.getCategories().indexOf(child);
                case Category __ -> indexOfChild = asCategory(parent).getTags().indexOf(child);
                case Tag __ -> indexOfChild = asTag(parent).getFeatureFiles().indexOf(child);
                default -> {
                }
            }
        } else {
            indexOfChild = -1;
        }
        return indexOfChild;
    }
}
