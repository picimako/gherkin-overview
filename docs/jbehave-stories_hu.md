## JBehave Story támogatás

A [JBehave](https://jbehave.org), a Cucumber mellett, egy széles körben ismert és használt BDD keretrendszer.
A Gherkin fájlok támogatásán felül rendelkezik egy saját fájltípussal és szintaxissal, a Story-kkal.

### Előfeltételek

Story fájlokhoz a [JBehave Support](https://plugins.jetbrains.com/plugin/7268-jbehave-support) ad támogatás,
ezért szükséges feltelepíteni, ha szeretnéd, hogy a Gherkin Overview ezeket a fájlokat is tudja kezelni. 

A JBehave Support telepítése opcionális, nem megléte nem akadályozza a hétköznapi Gherkin fájlok kezelését.

## Meta elemek kezelése

Bár a különálló meta text-eket érvényes meta-kként kezeli a JBehave Support, a JBehave maga azonban nem így tesz.
Meta elemek különálló kulcsként vagy kulcs-érték párként érvényesek.

A Story meta-król a Gherkin Overview által kezelt meta-kra való átalakítás az alábbiak szerint történik:

| Meta a .story fájlban           | Meta a bővítményben                               |
|---------------------------------|---------------------------------------------------|
| `Meta: @suite`                  | suite                                             |
| `Meta: @suite smoke`            | suite:smoke                                       |
| `Meta: @suite smoke regression` | suite:smoke regression                            |
| `Meta: smoke`                   | *Ilyen meta-k érvénytelenként vannak értelmezve.* |

A kategóriákhoz történő rendelés a beállításokban ezen értéktípusok alapján lehetséges.

## Több megegyező nevű Story fájl megkülönböztetése

Mivel a Story fájlok nem rendelkeznek olyan egyedi kulcsszóval, mint a `Feature` a Gherkin fájlokban, ha több Story fájl
létezik ugyanazon meta csomópont alatt, akkor a fájlútvonal alapján vannak megkülönböztetve.

## Az aktuális projektben előforduló Story meta-k összegyűjtése

A bővítmény beállításai alatt, a meta hozzárendelések összegyűjtése ugyanúgy történik, mint a Gherkin fájlok esetén.

Az eredmény táblázatban a Gherkin tag-ek és Story meta-k nincsenek megkülönböztetve, mivel valószínűtlen, hogy Cucumber
és JBehave is legyen használva ugyanazon projektben. Ily módon egyszerűbb a kezelésük is.
