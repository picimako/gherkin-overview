# Gherkin Overview

[![Gherkin Overview](https://img.shields.io/jetbrains/plugin/v/16716-gherkin-overview)](https://plugins.jetbrains.com/plugin/16716-gherkin-overview)

<!-- Plugin description -->
This JetBrains IDE plugin helps to better visualize the structure of test projects incorporating .feature files, for Cucumber and similar frameworks,
from the perspective of Gherkin tags.
<!-- Plugin description end -->

The documentation is available in the following languages:

<details open>
    <summary><b>English</b></summary>

The core idea is to provide a better overview of what Gherkin tags are available in a project, what Gherkin files they are contained by,
and with grouping tags into categories, make them easier to search.

It collects all .feature files from an open project, regardless of what BDD framework is used, and visualizes it in a custom tool window.

## Preconditions

Before installing this plugin, make sure to install the plugin called **Gherkin** as well, if it's not already installed (it is most probably bundled with the IDE).

To work with JBehave Story files, the [**JBehave Support**](https://plugins.jetbrains.com/plugin/7268-jbehave-support) plugin must be installed.

## Support for different BDD syntax

For common Gherkin files, this readme provides information, while for JBehave Stories (introduced in v0.2.0),
you can head over to the [JBehave Story support](docs/jbehave-stories_en.md) document.

## Gherkin Tags tool window

This is a custom tool window that displays the Gherkin tags, associated Gherkin files and related categories in a tree view, in the layouts detailed below.

**Without any grouping, showing data from the overall project**
```
Gherkin Tags                        <-- Root node. It is permanent.
    Test Suite                      <-- A Category of tags.
        smoke                       <-- Gherkin tag
            homepage_smoke.feature  <-- A .feature file.
        regression
            search_page.feature
        e2e
            landing_page.feature
    Device
        desktop
            landing_page.feature
        mobile
            search_page.feature
    ...
```

**Grouping by modules in the project**
```
Gherkin Tags                            <-- Root node. It is permanent.
    Module                              <-- A project module in the IDE.
        Test Suite                      <-- A Category of tags.
            smoke                       <-- Gherkin tag
                homepage_smoke.feature  <-- A .feature file.
            regression
                search_page.feature
            e2e
                landing_page.feature
    Module-2
        Device
            desktop
                landing_page.feature
            mobile
                search_page.feature
        ...
```

| No grouping                                                                | Grouping by modules                                    |
|----------------------------------------------------------------------------|--------------------------------------------------------|
| ![project_view_with_no_grouping](assets/project_view_with_no_grouping.PNG) | ![grouping_by_modules](assets/grouping_by_modules.PNG) |

In case a project doesn't have .feature/.story files, or it has some but there is no Gherkin tag used, the tool window simply displays the following message: *There is no Gherkin tag in this project.*

A **Category** is a group associated to tags. An @e2e and @regression tag may be associated with the Test Suite category, @Safari might be added to a category called Browser. It provides additional grouping in the tool window for easier search.

### Catch-all groups

Though some tags are assigned by default to certain categories on the application (IDE) level, and they can be further configured, there will still be tags that are not assigned to any category. For this purpose there is a permanent catch-all category to which tags, that are not assigned to any other category, will be assigned to.

In case the tool window's contents are not grouped by content roots, there on such category, while in case of grouping by content roots, each content root has its own **Other** category.

A similar logic is applied to content roots as well, where the catch-all content root is called **Rootless**, in case
a file is not part of any IDE content root. The difference here is that this root is not permanent, so if there is no Gherkin file that is place out of any content root, this group is not displayed.

### Statistics

In addition to the tree view itself, the nodes can display extra statistics about the number of Gherkin files, tags
and tag occurrence counts.

This extra information can be enabled/disabled under the ![view_icon](assets/tool_window_view_icon.PNG) toolbar menu. They display the following data:

| Node types           | Simplified stat.                                           | Detailed stat.                                         | Notes                                                                                                                                                                                                                                    |
|----------------------|------------------------------------------------------------|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Project/Content root | *X tags, Y .feature files*                                 | X distinct tags in Y .feature files                    | X: number of distinct tags in the project/content root<br>Y: number of feature files in the project/content root that actually contain tags. The overall number of .feature files in the project/content root may be the same or higher. |
| Category             | *(X)*                                                      | *X for Y distinct tags*                                | X: number of occurrences of all tags under this category in the associated project/content root                                                                                                                                          |
| Tag                  | *(X)*                                                      | *X in Y files*                                         | X: number of occurrences of this tag in the associated project/content root                                                                                                                                                              |
| Feature              | *(X)*                                                      | *X occurrence*                                         | X: number of occurrences of the parent tag in this file                                                                                                                                                                                  |
|                      | ![statistics_simplified](assets/statistics_simplified.PNG) | ![statistics_detailed](assets/statistics_detailed.PNG) |                                                                                                                                                                                                                                          |

### Search in the tree view

To make search easier in the tool window, you can simply start typing your keyword, and the tree view will highlight all matching, visible nodes, the same way search works in the IDE Project view.

### Tool window updates

Since changes can and do happen in the files and folders in the project, changes are reflected in the Tags tool window as well.

The tool window UI (and the underlying model) is updated when a Gherkin/Story file's content has changed, or the file has be removed,
in every other case (e.g. file rename, Git revert, folder copy, ...) the whole model is rebuilt from scratch, also collapsing the UI tree.

### Context menu actions

Since v1.2.0, a context menu action is available on Tag nodes. It can delete all occurrences of the selected tag/meta from all affected files.

NOTE: minor formatting adjustments might be necessary after using this action.

![delete_all_occurrences_context_action](assets/delete_all_occurrences_context_action.png)

## Settings

Within `Settings > Tools > Gherkin Overview` there are additional customization options.

Gherkin tags can be mapped to existing or new categories based on which the tool window will display them, meaning you can
define the Gherkin tags that your test projects actually use, to tailor them to your needs.

There are two levels of such mappings: application and project.

Application-level mappings are applied to all open projects in the IDE, while project-level mappings override application-level
ones, and are applied only to the current project. This way, if you have multiple test projects, you can customize the mappings, so that they can use common mappings as well as their own project specific ones.

The plugin comes with a [default set of application-level mappings](src/main/resources/mapping/default_app_level_mappings.properties) that can be modified any time.

In the settings panel you can find a customizable table for both levels.

The lists of tags that are assigned to given categories can be specified as comma separated lists of strings. Under the hood
tags are stored without the leading @ symbol, so make sure the tag values here are specified without them. This makes
the UI less cluttered, and it is easier to specify them this way.

If you happen to make a mistake during configuration, there is a **Reset to default** option that resets the application-level mappings to the aforementioned default set.

### Using project-level mappings

To use the project-level ones, first you have to check the **Use project level category-tag mapping** checkbox.

To better understand how application and project-level values are merged and handled, you can find a few examples here:

| Application                        | Project                              | Final, merged mapping                                          |
|------------------------------------|--------------------------------------|----------------------------------------------------------------|
| Test Suite -> smoke,regression,e2e |                                      | Test Suite -> smoke,regression,e2e                             |
|                                    | Device -> mobile,desktop             | Device -> mobile,desktop                                       |
| Test Suite -> smoke,regression,e2e | Device -> mobile,desktop             | Test Suite -> smoke,regression,e2e<br>Device -> mobile,desktop |
| Test Suite -> smoke,regression,e2e | Test Suite -> regression,healthcheck | Test Suite -> smoke,regression,e2e,healthcheck                 |
| Test Suite -> smoke,regression,e2e | Test Pack -> e2e                     | Test Suite -> smoke,regression<br>Test Pack -> e2e             |

**NOTE:** mapping the same tag (either explicit ones, or regex based tags with overlapping match results) to multiple different categories
should be avoided as it may cause weird issues in the tool window's tree view.

### Collecting Gherkin tags from the current project

Defining your own mappings is quite useful, but it would still be quite a hassle to collect all the distinct Gherkin tags from your project
to be able to map them.

The bottom section of the plugin's settings page is aimed to help with that. Once IDE finished indexing, just click on the ![collect_tags_from_project](assets/collect_tags_from_project_button.PNG) button. Once it's done, it shows you the tags separated into categories.
If a tag is already mapped to a category, then it is shown accordingly, so you don't necessarily have to deal with their categorization, otherwise unmapped tags are put into
a single table cell.

When there is indexing in progress, and you hit the button, you get a message saying ![tags_cant_be_collected](assets/tags_cant_be_collected_during_indexing.PNG).

If the collection cannot start when there is no apparent indexing still ongoing, there might be additional indexing queued up by the IDE that will happen when the Settings window is closed. In that case reopening the settings can help mitigate the problem.

### Regex values

Beside exact tag values, regex patterns can also be bound to categories. Their values have to start with a hashmark (#).
That symbol identifies that the value has to be treated as a regex pattern.

One example is available in the application level mappings for Jira ticket identifiers: `#^[A-Z]+-[0-9]+$`.

One or more such patterns can be assigned to a category. A category can be assigned exact tag values and regex patterns as well, in a mixed manner.

## Distinguishing multiple Gherkin files with the same name

It may be a rare case, but it is still possible that a project contains more than one Gherkin file with the same name, even containing
at least some of the same tags.

In that case, .feature files named for instance `homepage_smoke.feature` would be displayed as:

```
- @Smoke
    - homepage_smoke.feature
    - homepage_smoke.feature
    - homepage_smoke.feature
```

That is not really helpful. To help differentiate between those files, the following mechanism is implemented:
- if the first Feature keyword's text is different in each of these files, then those values are displayed, e.g.:

```
- @Smoke
    - homepage_smoke.feature [Generic smoke test]
    - homepage_smoke.feature [Homepage analytics smoke]
    - homepage_smoke.feature [Homepage search smoke]
```

This may be an indicator that you need to give more descriptive names to your .feature files.

But, if Feature names are not distinct, then instead they are differentiated by their relative paths to the project's
root folder:

```
- @Smoke
    - homepage_smoke.feature [aModule/src/main/resources/features]
    - homepage_smoke.feature [/]  <-- It means, the file is located in the project's root.
    - homepage_smoke.feature [another/folder]
```

## Export / Import

Since v0.3.0, application-level category-tags mappings can be exported via the IDE's **Export Settings...** dialog, along with other plugin and IDE settings.

## Licensing

This project is licensed under the terms of Apache Licence Version 2.0.

## Acknowledgements

A special thank you to [Limpek07](https://github.com/Limpek07) for the many brainstorming sessions, ideas and testing efforts.
</details>

<details>
    <summary><b>Magyar - Hungarian</b></summary>

A bővítmény alapötlete, hogy jobb átláthatóságot adjon arról, egy projektben milyen Gherkin tag-ek vannak, azok mely fájlokban
vannak használva, illetve a tag-ek kategóriákba sorolásával segítse azok könnyebb kereshetőségét.

A használt BDD keretrendszertől függetlenül összegyűjti az egy projektben létező minden .feature fájlt,
amiket aztán egy egyedi tool window-ban jelenít meg.

## Előfeltételek

A bővítmény telepítése előtt szükséges a **Gherkin** nevű bővítmény telepítése is, ha még nem lenne (valószínűleg az IDE-vel együtt telepítésre kerül).

A JBehave Story fájlok támogatásához szükséges a [**JBehave Support**](https://plugins.jetbrains.com/plugin/7268-jbehave-support) bővítmény telepítése.

## Különféle BDD szintaxisok támogatása

A Gherkin fájlokkal kapcsolatos információkat e dokumentum részletezi, míg a JBehave Story-kat (v0.2.0 óta) a [JBehave Story support](docs/jbehave-stories_hu.md)
dokumentum.

## Gherkin Tags tool window

Ez egy egyedi tool window, ami a Gherkin tag-eket, az őket tartalmazó Gherkin fájlokat és kapcsolódó kategóriákat egy fa nézetben jeleníti meg,
a lent részletezett elrendezésben.

**Csoportosítás nélkül, a teljes projektben elérhető adatokat megjelenítve**
```
Gherkin Tags                        <-- A fa legkülső, állandó pontja.
    Test Suite                      <-- Tag-ek egy kategóriája.
        smoke                       <-- Gherkin tag
            homepage_smoke.feature  <-- Egy .feature fájl.
        regression
            search_page.feature
        e2e
            landing_page.feature
    Device
        desktop
            landing_page.feature
        mobile
            search_page.feature
    ...
```

**Csoportosítás a projekt moduljai alapján**
```
Gherkin Tags                            <-- A fa legkülső, állandó pontja.
    Module                              <-- A projekt egy modulja.
        Test Suite                      <-- Tag-ek egy kategóriája.
            smoke                       <-- Gherkin tag
                homepage_smoke.feature  <-- Egy .feature fájl.
            regression
                search_page.feature
            e2e
                landing_page.feature
    Module-2
        Device
            desktop
                landing_page.feature
            mobile
                search_page.feature
        ...
```

| Csoportosítás nélkül                                                       | Csoportosítás modulonként                              |
|----------------------------------------------------------------------------|--------------------------------------------------------|
| ![project_view_with_no_grouping](assets/project_view_with_no_grouping.PNG) | ![grouping_by_modules](assets/grouping_by_modules.PNG) |

Ha egy projekt nem tartalmaz .feature/.story fájlt, vagy tartalmaz, de nincs bennük tag használva, a tool window-ban a következő felirat jelenik meg:
*There is no Gherkin tag in this project*.

Egy **Kategória** egy tag-ekhez rendelt csoportot jelent. Egy @e2e és @regression tag tartozhat egy Test Suite,
míg a @Safari a Browser kategóriához. Ez a fajta csoportosítás egyszerűbb keresést biztosít a tool window-ban.

### Közös csoportok

Habár bizonyos tag-ek már telepítéskor kategóriákhoz vannak rendelve az alkalmazás (IDE) szintjén,
és azok még tovább konfigurálhatók, lesznek egyetlen kategóriához sem rendelt tag-ek is.
Erre az esetre lett létrehozva egy állandó kategória, hogy legyen hol megjeleníteni a kategóriához nem tartozó tag-eket.

Ha a tool window tartalmára nincs csoportosítás kiválasztva, akkor egyetlen ilyen kategória jelenik meg Other névvel,
míg a content root-ok szerinti csoportosítás esetén minden content root-hoz megjelenik a saját **Other** nevű kategóriája.

Hasonló logika érvényes az olyan fájlokra, amelyek nem tartoznak egy content root-hoz sem, ezek a közös **Rootless** nevű kategóriában jelennek meg.
A különbség annyi, hogy ha nincs content root-hoz nem tartozó Gherkin fájl, a Rootless kategória nem jelenik meg.

### Statisztika

A fa nézeten felül, a csomópontok további statisztikai adatokat is képesek megjeleníteni a Gherkin fájlok számát,
és a tag-ek számát és előfordulásukat illetően.

Ez ez extra információ az eszköztár ![view_icon](assets/tool_window_view_icon.PNG) ikonjával kapcsolható ki/be, és az alábbi adatokat jeleníti meg:

| Csomópont típusok    | Egyszerűsített statisztika                                 | Részletes statisztika                                  | Notes                                                                                                                                                                                                                                    |
|----------------------|------------------------------------------------------------|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Project/Content root | *X tags, Y .feature files*                                 | X distinct tags in Y .feature files                    | X: number of distinct tags in the project/content root<br>Y: number of feature files in the project/content root that actually contain tags. The overall number of .feature files in the project/content root may be the same or higher. |
| Category             | *(X)*                                                      | *X for Y distinct tags*                                | X: number of occurrences of all tags under this category in the associated project/content root                                                                                                                                          |
| Tag                  | *(X)*                                                      | *X in Y files*                                         | X: number of occurrences of this tag in the associated project/content root                                                                                                                                                              |
| Feature              | *(X)*                                                      | *X occurrence*                                         | X: number of occurrences of the parent tag in this file                                                                                                                                                                                  |
|                      | ![statistics_simplified](assets/statistics_simplified.PNG) | ![statistics_detailed](assets/statistics_detailed.PNG) |                                                                                                                                                                                                                                          |

### Keresés a fában

Hogy a tool window-ban történő keresés egyszerűbb legyen, csak kezdd el írni a keresett kifejezést.
A fában ekkor, az IDE Projekt nézetéhez hasonlóan, kijelölésre kerül minden vele egyező, és látható, csomópont.

### A tool window tartalmának frissítése

A projektfájlokban és -mappákban történő változásokat a tool window-ban megjelenített adatok is követik és tükrözik.

A tool window adatai (az alsóbb szintű adatmodell) és a grafikus felület azonnal, szimplán frissítésre kerül,
amint egy Gherkin/Story fájl megváltozik vagy a fájl törlésre kerül. Minden más esetben
(pl. fájl átnevezése, Git revert, mappa másolása, ...), a teljes adatmodell újjáépül, a fa nézet pedig összecsukódik.

### Helyi menü lehetőségek

Az 1.2.0 verzió óta elérhető egy helyi menü opció a Tag csomópontokon. Ez törli a kiválasztott tag/meta összes előfordulásátt az összes érintett fájlból.

Megjegyzés: az érintett fájlok további kisebb formázása szükséges lehet az opció használata után.

![delete_all_occurrences_context_action](assets/delete_all_occurrences_context_action.png)

## Beállítások

A `Settings > Tools > Gherkin Overview` alatt további testreszabási lehetőségek érhetőek el.

A Gherkin tag-ek hozzárendelhetőek már létező vagy új kategóriákhoz, amik alapján a tool window megjeleníti őket.
Így csoportokba szervezheted a projektedben ténylegesen használt tag-eket, hogy a saját igényeidre szabd őket.

Ezek két szinten vannak jelen: alkalmazás és projekt.

Az alkalmazás szintű hozzárendelések minden megnyitott projektre érvényesek, míg a projekt szintűek felülírják
az alkalmazás szintűeket, és csak az épp aktuális projektre érvényesek. Így oly módon lehetséges testreszabni őket,
hogy több teszt projekt esetén mindegyik használni tudja az alkalmazás szintűeket és a saját projektjéhez tartozó értékeket is.

A bővítmény telepítése után elérhető az [alkalmazás szintű hozzárendelések egy kezdő szettje](src/main/resources/mapping/default_app_level_mappings.properties),
amelyek bármikor módosíthatóak.

A beállítások panelen mind a két szintre elérhető egy-egy testreszabható táblázat.

Egy adott kategóriához rendelt tag-ek listája elemek vesszővel elválasztott felsorolásaként adható meg.
A bővítmény a tag-eket a kezdő @ karakter nélkül tárolja, ezért fontos, hogy a tag-eket a @ nélkül szükséges megadni.
Ennek köszönhetően a felület kevésbé zsúfolt, illetve könnyebb őket megadni.

Ha valamilyen hibát vétenél a konfigurálás közben, a **Reset to default** gombbal alaphelyzetbe állíthatóak az alkalmazás szintű hozzárendelések.

### Projekt szintű hozzárendelések

A projekt szintű beállításokhoz először a **Use project level category-tag mapping** dobozt kell bepipálni.

Azt, hogy hogyan vannak a különböző szintű hozzárendelések összefésülve és kezelve, az alábbi példák mutatják be:

| Alkalmazás                         | Projekt                              | Végső, összefésült hozzárendelések                             |
|------------------------------------|--------------------------------------|----------------------------------------------------------------|
| Test Suite -> smoke,regression,e2e |                                      | Test Suite -> smoke,regression,e2e                             |
|                                    | Device -> mobile,desktop             | Device -> mobile,desktop                                       |
| Test Suite -> smoke,regression,e2e | Device -> mobile,desktop             | Test Suite -> smoke,regression,e2e<br>Device -> mobile,desktop |
| Test Suite -> smoke,regression,e2e | Test Suite -> regression,healthcheck | Test Suite -> smoke,regression,e2e,healthcheck                 |
| Test Suite -> smoke,regression,e2e | Test Pack -> e2e                     | Test Suite -> smoke,regression<br>Test Pack -> e2e             |

**Megjegyzés:** ugyanazon tag (legyen az konkrét tag név alapján, vagy regex alapú tag-ek egymást átfedő eredményekkel)
több különböző kategóriához történő hozzárendelését érdemes elkerülni, mert furcsa megjelenítési problémákat okozhat a tool window-ban.

### Az aktuális projektben előforduló Gherkin tag-ek összegyűjtése

Saját hozzárendelések készítése kifejezetten hasznos, de segítség nélkül még mindig problémás lehet
összegyűjteni egy projektben előforduló összes Gherkin tag-et, hogy aztán kategóriákba rendezhessük őket.

A beállítások panel alsó szekciója ebben hivatott segíteni. Amikor az IDE befejezte az indexelést, kattints a ![collect_tags_from_project](assets/collect_tags_from_project_button.PNG) gombra!
Amikor a művelet befejeződött, az összegyűjtött tag-eket kategóriába rendezve fogja megjeleníteni.
Ha egy tag már hozzá van rendelve egy kategóriához, akkor az aszerint jelenik meg, így nem feltétlen szükséges foglalkozni azok kategorizálásával.
Egyébként, a kategóriához nem rendelt tag-ek egy közös cellában jelennek meg.

Ha a gombra kattintáskor egy indexelés épp folyamatban van, a következő üzenet jelenik meg: ![tags_cant_be_collected](assets/tags_cant_be_collected_during_indexing.PNG)

Ha a tag-ek összegyűjtése nem kezdődik el annak ellenére, hogy látszólag nem történik indexelés, lehetséges,
hogy a háttérben az IDE már sorbaállított több indexelési feladatot, amik akkor fognak lefutni,
amikor a Settings ablak bezáródik. Ilyenkor a beállítások újranyitása megoldhatja a problémát.

### Regex értékek

Konkrét tag-ek mellett reguláris kifejezések is megadhatók a kategóriákban. Ezek értékei kettőskereszttel (#) kell, hogy kezdődjenek.

Egy példa erre az alkalmazás szintű értékek esetén a Jira jegy azonosítók: `#^[A-Z]+-[0-9]+$`.

Egy vagy több ilyen kifejezés is megadható egy adott kategóriában. Egy kategóriához pedig tag nevek és reguláris kifejezések vegyesen is hozzárendelhetőek.

## Több megegyező nevű Gherkin fájl megkülönböztetése

Bár valószínűleg ritka eset, de lehetséges, hogy egy projektben ugyanazzal a névvel több fájl is létezik, akár több közös tag-et is tartalmazva.

Ebben az esetben a pl. `homepage_smoke.feature` nevű fájlok így jelennének meg:

```
- @Smoke
    - homepage_smoke.feature
    - homepage_smoke.feature
    - homepage_smoke.feature
```

Ez nem igazán hasznos, így ezen fájlok megkülönböztetésére a következő logika lett implementálva.

Ha az első Feature kulcsszó szövege különbözik ezekben a fájlokban, akkor ez a szöveg jelenik meg, pl.:

```
- @Smoke
    - homepage_smoke.feature [Generic smoke test]
    - homepage_smoke.feature [Homepage analytics smoke]
    - homepage_smoke.feature [Homepage search smoke]
```

Ez jelezheti azt is, hogy ezeknek a .feature fájloknak beszédesebb neveket kellene adni.

Ha viszont a Feature nevek megegyeznek, akkor a projekt gyökeréhez viszonyított relatív útvonallal vannak megkülönböztetvE:

```
- @Smoke
    - homepage_smoke.feature [aModule/src/main/resources/features]
    - homepage_smoke.feature [/]  <-- A fájl a projekt gyökerében található.
    - homepage_smoke.feature [another/folder]
```

## Export / Import

A 0.3.0 verzió óta az alkalmazás szintű hozzárendelések kiexportálhatók az IDE **Export Settings...** ablakában,
további bővítmények és az IDE beállításaival együtt.

## Licenszelés

A projektre az Apache Licence Version 2.0 licensz érvényes.

## Köszönetnyilvánítás

Külön köszönet illeti [Limpek07](https://github.com/Limpek07)-et a sok agyviharzós szeánsz, az ötletei és a tesztelési segítség kapcsán.
</details>
