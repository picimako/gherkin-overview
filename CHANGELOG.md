<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Gherkin Overview Changelog

## [Unreleased]

## [1.8.0]
### Changed
- New supported IDE version range: 2023.2.8-2024.2.0.2

## [1.7.0]
### Changed
- New supported IDE version range: 2023.1.6-2024.2-EAP.
- Updated plugin dependencies.

## [1.6.0]
### Changed
- New supported IDE version range: 2022.3.3-2024.1-EAP.
- Updated plugin dependencies.

## [1.5.1]

### Fixed
- [12](https://github.com/picimako/gherkin-overview/issues/12): Fixed the issue that an exception was thrown when the tag occurrence counts started to be updated
for an invalid or non-existent file.

## [1.5.0]

### Changed
- New supported IDE version range: 2022.2-2023.3
- Plugin configuration and dependency updates.

## [1.4.0]

### Added
- [#3](https://github.com/picimako/gherkin-overview/issues/3): Added a new toolbar button in the Tags tool window. When a Gherkin tag is under the caret
in an editor, it locates and selects the corresponding Gherkin tag node in the tool window. 

### Fixed
- Fix a potential class not found exception due to the JBehave icon.
- [#7](https://github.com/picimako/gherkin-overview/issues/7): The root node in the tool window displays a customized statistics message depending on if the project contains
only Gherkin files, only Story files, or both.

## [1.3.0]

### Changed
- Removed support for IJ-2021.3, and added support for IJ-2023.2.
- Configuration updates and optimization under the hood.

## [1.2.2]

### Added
- The effects of the **Delete All Occurrences of This Tag** context menu action can now be reverted via the IDE local history.
It also works with Ctrl+Z and similar Undo actions.

### Changed
- Removed support for IJ-2021.2.
- Added support for IJ-2023.1.
- Updated JBehave Support plugin dependency to support IJ-2023.1.
- Reworked the plugin build configuration to be based on the intellij-platform-plugin-template.

## [1.2.1]

### Changed
- Updated JBehave Support plugin dependency to support IJ-2022.3.
- Updated gradle-intellij-plugin version.

## [1.2.0]

### Added
- Removed support for IJ-2021.1.
- Added a context menu action to Tag nodes that can delete all occurrences of the selected tag.

### Changed
- Upgraded library versions: gradle-intellij-plugin to 1.7.0, gradle to 7.4, jbehave-support-plugin 1.58

## [1.1.0]

### Changed
- Add support for IJ 2022.1 EAP.

## [1.0.0]

### Changed
- Upgraded a couple of dependency versions
- Replaced Project object with a no-op Disposable for psi tree change listener since Project type Disposables are not allowed as parents
- Refined light service definitions, and project description

## [0.3.0]

### Added
- Application-level mappings are now exportable via the IDE's **Export Settings...** dialog.
- Added a **Reset to default** button to the application-level mappings in the plugin settings.
- Added extra file and folder listener to prevent the tool window nodes from breaking when changes like file rename,
  move, rollback, etc. happens.
  - **NOTE:**
    
    Due to the nature of this plugin, that it supports any kind of projects with Gherkin files,
    limiting the directory and file change events to ones only in test resources is not achievable since it would be
    difficult or even impossible to handle all kinds of languages and project structures.
    
    Thus, more frequent tool window updates and model rebuilds are expected mostly during various folder related changes.

### Changed
- Added an alternate help tooltip text in the plugin settings for the case when JBehave story files are available too.
- Further adjustments to ensure the plugin doesn't break when the JBehave Support plugin is not installed, or disabled.

### Fixed
- Fixed an issue that the default enabled/disabled state of the project-level mappings component in the plugin settings
didn't reflect the state of the **Use project level category-tag mapping** checkbox.
- Fixed a cause of `NullPointerException` in `TagOccurrencesRegistry`.

## [0.2.0]

### Added
- Added support for [JBehave](https://jbehave.org) Story files:
  - .story files are now also recognized in projects, given that the [JBehave Support](https://plugins.jetbrains.com/plugin/16716-gherkin-overview) plugin is installed, since that
  plugin provides the language support for JBehave Story files.
  - Renamed the `Gherkin Tags` tool window to simply `Tags` to reflect that it is not just Gherkin files that the tool
  window can display.
  - In the tool window, the root element's name reflects whether Gherkin and/or JBehave Story files are available in the
  project.

### Changed
- The tree model in the Tags tool window is rebuilt when either the application or the project-level mapping
has been changed in Settings. This aims to provide a smoother user experience by not having to restart the IDE or
turn to other workarounds to reload the tool window, so that it displays data with the up-to-date mappings.

## [0.1.0]

Initial release
