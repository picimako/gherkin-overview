//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import icons.CucumberIcons;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GherkinTagTree.GherkinTagsNodeRenderer}.
 */
final class GherkinTagsNodeRendererTest extends GherkinOverviewTestBase {
    private GherkinTagTree tree;
    private GherkinTagTree.GherkinTagsNodeRenderer renderer;

    @BeforeEach
    void setUp() {
        tree = new GherkinTagTree(new ProjectSpecificGherkinTagTreeModel(getProject()), getProject());
        renderer = new GherkinTagTree.GherkinTagsNodeRenderer(getProject());
    }

    @Test
    void renderCellForModelDataRoot() {
        renderer.customizeCellRenderer(tree, new ModelDataRoot(getProject()), true, true, false, 0, false);

        assertThat(renderer.getIcon()).isEqualTo(PlatformIcons.FOLDER_ICON);
        assertThat(renderer.getCharSequence(true)).isEqualTo("Gherkin Tags");
    }

    @Test
    void renderCellForModule() {
        renderer.customizeCellRenderer(tree, ContentRoot.createModule("module name", getProject()), true, true, false, 0, false);

        assertThat(renderer.getIcon()).isEqualTo(AllIcons.Actions.ModuleDirectory);
        assertThat(renderer.getCharSequence(true)).isEqualTo("module name");
    }

    @Test
    void renderCellForCategory() {
        renderer.customizeCellRenderer(tree, new Category("Test Suite", getProject()), true, true, false, 0, false);

        assertThat(renderer.getIcon()).isEqualTo(PlatformIcons.LIBRARY_ICON);
        assertThat(renderer.getCharSequence(true)).isEqualTo("Test Suite");
    }

    @Test
    void renderCellForTag() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        renderer.customizeCellRenderer(tree, new Tag("regression", theGherkin, getProject()), true, true, false, 0, false);

        assertThat(renderer.getIcon()).isEqualTo(AllIcons.Gutter.ExtAnnotation);
        assertThat(renderer.getCharSequence(true)).isEqualTo("regression");
    }

    @Test
    void renderCellForGherkinFile() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        FeatureFile featureFile = new FeatureFile(theGherkin, "parent", getProject());

        renderer.customizeCellRenderer(tree, featureFile, true, true, true, 0, false);

        assertThat(renderer.getIcon()).isEqualTo(CucumberIcons.Cucumber);
        assertThat(renderer.getCharSequence(true)).isEqualTo("the_gherkin.feature");
    }
}
