# Frequent error messages

## No NetworkFactoryService providers found

This most likely happened because you tried to read a file, but didn't have any implementation of the IIDM API in your classpath.
Use the implementation provided by PowSyBl / a third party, or write your own.

### Fixing the issue by using powsybl-core implementation

If you wish to use powsybl-core in-memory implementation, add the following to your `pom.xml` in the `<dependencies>` section:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-impl</artifactId>
    <version>$version$</version>
</dependency>
```

:::{admonition} Using the correct version
:class: tip dropdown

Replace `$version$` by the version of this impl that will work with other PowSyBl packages.
A comprehensive table of matching versions can be found at [powsybl-dependencies](https://github.com/powsybl/powsybl-dependencies?tab=readme-ov-file#powsybl-included-repositories-versions);
use the `powsybl-core` version that matches for you.
:::

You should also make sure that you have an implementation of `SerDe` if you are trying to read a file. See [adding SerDe](#example-for-an-iidm-file).

## Unsupported file format or invalid file

You are most likely missing in your classpath the implementation of `com.powsybl.iidm.network.Importer` corresponding to the file you are trying
to import (`XmlImporter`, `CgmesImporter`, etc.). Use the implementation provided by PowSyBl / a third party, or write your own.

### Example for an IIDM file

If you wish to import an IIDM file using powsybl-core implementation, add the following to your `pom.xml` in the `<dependencies>` section:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-serde</artifactId>
    <version>$version$</version>
</dependency>
```

:::{admonition} Using the correct version
:class: tip dropdown

Replace `$version$` by the version of this impl that will work with other PowSyBl packages.
A comprehensive table of matching versions can be found at [powsybl-dependencies](https://github.com/powsybl/powsybl-dependencies?tab=readme-ov-file#powsybl-included-repositories-versions);
use the `powsybl-core` version that matches for you.
:::

