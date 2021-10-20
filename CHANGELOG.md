# Changelog

### 0.3.0

#### NEW
- Application-level mappings are now exportable via the IDE's **Export Settings...** dialog.
- Added a **Reset to default** button to the application-level mappings in the plugin settings.
- Added extra file and folder listener to prevent the tool window nodes from breaking when changes like file rename,
  move, rollback, etc. happens.
  - **NOTE:**
    
    Due to the nature of this plugin, that it supports any kind of projects with Gherkin files,
    limiting the directory and file change events to ones only in test resources is not achievable since it would be
    difficult or even impossible to handle all kinds of languages and project structures.
    
    Thus, more frequent tool window updates and model rebuilds are expected mostly during various folder related changes.

#### ENHANCEMENT
- Added an alternate help tooltip text in the plugin settings for the case when JBehave story files are available too.
- Further adjustments to ensure the plugin doesn't break when the JBehave Support plugin is not installed, or disabled.

#### BUGFIX
- Fixed an issue that the default enabled/disabled state of the project-level mappings component in the plugin settings
didn't reflect the state of the **Use project level category-tag mapping** checkbox.
- Fixed a cause of `NullPointerException` in `TagOccurrencesRegistry`.

### 0.2.0

#### NEW
- Added support for [JBehave](https://jbehave.org) Story files:
  - .story files are now also recognized in projects, given that the [JBehave Support](https://plugins.jetbrains.com/plugin/16716-gherkin-overview) plugin is installed, since that
  plugin provides the language support for JBehave Story files.
  - Renamed the `Gherkin Tags` tool window to simply `Tags` to reflect that it is not just Gherkin files that the tool
  window can display.
  - In the tool window, the root element's name reflects whether Gherkin and/or JBehave Story files are available in the
  project.

#### ENHANCEMENT
- The tree model in the Tags tool window is rebuilt when either the application or the project-level mapping
has been changed in Settings. This aims to provide a smoother user experience by not having to restart the IDE or
turn to other workarounds to reload the tool window, so that it displays data with the up-to-date mappings.

### 0.1.0

Initial release
