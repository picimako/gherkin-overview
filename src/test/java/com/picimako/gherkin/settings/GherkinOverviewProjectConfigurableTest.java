//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.settings;

import static com.picimako.gherkin.ToolWindowTestSupport.getToolWindowModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Optional;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GherkinOverviewProjectConfigurable}.
 */
final class GherkinOverviewProjectConfigurableTest extends MediumBasePlatformTestCase {

    private GherkinOverviewProjectConfigurable configurable;

    @BeforeEach
    void setUp() {
        configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
    }

    @AfterEach
    void tearDown() {
        invokeAndWait(() -> {
            configurable.resetToDefault();
            configurable.disposeUIResources();
        });
    }

    //createComponent

    @Test
    void createComponent() {
        var component = getComponent();
        assertThat(component.isUseProjectLevelMappings()).isFalse();

        assertThat(component.getApplicationLevelMappings())
            .contains(
                new CategoryAndTags("Device", "desktop,tablet,mobile,iphone,ipad,android,winphone,tab,device"),
                new CategoryAndTags("Browser", "ie,ie10,ie11,edge,internetexplorer,edge,firefox,ff,chrome,googlechrome,opera,safari,browser")
            ).doesNotContain(new CategoryAndTags("Breakpoint", "small"));

        assertThat(component.getProjectLevelMappings()).isEmpty();
    }

    //isModified

    @Test
    void isNotModifiedByDefault() {
        assertThat(configurable.isModified()).isFalse();
    }

    @Test
    void modifiedWhenUsingProjectLevelMappingsIsEnabled() {
        getComponent().setUseProjectLevelMappings(true);

        assertThat(configurable.isModified()).isTrue();
    }

    @Test
    void modifiedWhenUsingApplicationLevelMappings() {
        getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));

        assertThat(configurable.isModified()).isTrue();
    }

    @Test
    void modifiedWhenUsingProjectLevelMappings() {
        getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));
        assertThat(configurable.isModified()).isTrue();
    }

    //apply

    @Test
    void appliesSettingsWithoutProjectLevelMappings() {
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        getComponent().setApplicationLevelMappings(List.of(breakpoint));
        getComponent().setProjectLevelMappings(List.of(media));

        invokeAndWait(configurable::apply);

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

    @Test
    void appliesSettingsWithProjectLevelMappings() {
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        getComponent().setApplicationLevelMappings(List.of(breakpoint));
        getComponent().setProjectLevelMappings(List.of(media));
        getComponent().setUseProjectLevelMappings(true);

        invokeAndWait(configurable::apply);

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

    @Test
    void rebuildsModelIfAppLevelMappingsChanged() {
        getFixture().configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        invokeAndWait(configurable::apply);

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    @Test
    void rebuildsModelIfProjectLevelMappingsChanged() {
        getFixture().configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        getComponent().setUseProjectLevelMappings(true);
        getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        invokeAndWait(configurable::apply);

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    @Test
    void doesntRebuildModelIfNoMappingHasChanged() {
        getFixture().configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        invokeAndWait(configurable::apply);

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");
    }

    //reset

    @Test
    void resetsSettings() {
        getComponent().setUseProjectLevelMappings(true);
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        getComponent().setApplicationLevelMappings(List.of(breakpoint));
        getComponent().setProjectLevelMappings(List.of(breakpoint));

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

    private GherkinOverviewComponent getComponent() {
        return configurable.getComponent();
    }
}
