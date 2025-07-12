//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import static com.picimako.gherkin.ToolWindowTestSupport.getToolWindowModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

import java.util.List;
import java.util.Optional;

/**
 * Unit test for {@link GherkinOverviewProjectConfigurable}.
 */
public class GherkinOverviewProjectConfigurableTest extends MediumBasePlatformTestCase {

    private GherkinOverviewProjectConfigurable configurable;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
    }

    @Override
    public void tearDown() throws Exception {
        configurable.resetToDefault();
        configurable.disposeUIResources();
        super.tearDown();
    }

    //createComponent

    public void testCreateComponent() {
        var component = configurable.getComponent();
        assertThat(component.isUseProjectLevelMappings()).isFalse();

        assertThat(component.getApplicationLevelMappings())
            .contains(
                new CategoryAndTags("Device", "desktop,tablet,mobile,iphone,ipad,android,winphone,tab,device"),
                new CategoryAndTags("Browser", "ie,ie10,ie11,edge,internetexplorer,edge,firefox,ff,chrome,googlechrome,opera,safari,browser")
            ).doesNotContain(new CategoryAndTags("Breakpoint", "small"));

        assertThat(component.getProjectLevelMappings()).isEmpty();
    }

    //isModified

    public void testIsNotModifiedByDefault() {
        assertThat(configurable.isModified()).isFalse();
    }

    public void testModifiedWhenUsingProjectLevelMappingsIsEnabled() {
        configurable.getComponent().setUseProjectLevelMappings(true);

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedWhenUsingApplicationLevelMappings() {
        configurable.getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedWhenUsingProjectLevelMappings() {
        configurable.getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));
        assertThat(configurable.isModified()).isTrue();
    }

    //apply

    public void testAppliesSettingsWithoutProjectLevelMappings() {
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(media));

        configurable.apply();

        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(getProject());
        var registry = TagCategoryRegistry.getInstance(getProject());

        assertSoftly(s -> {
            s.assertThat(appSettings.mappings).containsExactly(breakpoint);
            s.assertThat(projectSettings.useProjectLevelMappings).isFalse();
            s.assertThat(projectSettings.mappings).containsExactly(media);
            s.assertThat(registry.categoryOf("small")).isEqualTo("Breakpoint");
            s.assertThat(registry.categoryOf("image")).isNull();
        });
    }

    public void testAppliesSettingsWithProjectLevelMappings() {
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(media));
        configurable.getComponent().setUseProjectLevelMappings(true);

        configurable.apply();

        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(getProject());
        var registry = TagCategoryRegistry.getInstance(getProject());

        assertSoftly(s -> {
            s.assertThat(appSettings.mappings).containsExactly(breakpoint);
            s.assertThat(projectSettings.useProjectLevelMappings).isTrue();
            s.assertThat(projectSettings.mappings).containsExactly(media);
            s.assertThat(registry.categoryOf("small")).isEqualTo("Breakpoint");
            s.assertThat(registry.categoryOf("image")).isEqualTo("Media");
        });
    }

    public void testRebuildsModelIfAppLevelMappingsChanged() {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        configurable.getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    public void testRebuildsModelIfProjectLevelMappingsChanged() {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        configurable.getComponent().setUseProjectLevelMappings(true);
        configurable.getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    public void testDoesntRebuildModelIfNoMappingHasChanged() {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");
    }

    //reset

    public void testResetsSettings() {
        configurable.getComponent().setUseProjectLevelMappings(true);
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(breakpoint));

        configurable.reset();

        assertSoftly(s -> {
            s.assertThat(getComponent().isUseProjectLevelMappings()).isFalse();
            s.assertThat(getComponent().getApplicationLevelMappings()).doesNotContain(breakpoint);
            s.assertThat(getComponent().getProjectLevelMappings()).isEmpty();
        });
    }

    //Helper methods

    private void validateCategories(ModelDataRoot model, String nonEmptyCategoryName, String emptyCategoryName) {
        Optional<Category> nonEmptyCategory = model.findCategory(nonEmptyCategoryName);
        assertThat(nonEmptyCategory).isPresent();
        assertThat(nonEmptyCategory.get().getTags()).hasSize(2);
        assertThat(model.findCategory(emptyCategoryName)).isEmpty();
    }
}
