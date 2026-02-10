# Frequent error messages

If you wish to use the latest version of PowSyBl's library without having to specify versions, you should have a look at [powsybl-dependencies](https://github.com/powsybl/powsybl-dependencies)
and related examples.

### No NetworkFactoryService providers found

This most likely happened because you tried to read a file, but you didn't have any implementation of the interface used for reading those files.
Use the implementation provided by PowSyBl / a third party, or write your own.

#### Fix missing impl

If you wish use to use PowSyBl's implementation, add the following to your `pom.xml` in the `<dependencies>` section:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-impl</artifactId>
    <version>$version$</version>
</dependency>
```

Please replace `$version$` by the version of this impl that will work with other PowSyBl packages.
A comprehensive table of matching versions can be found at [powsybl-dependencies](https://github.com/powsybl/powsybl-dependencies?tab=readme-ov-file#powsybl-included-repositories-versions);
use the `powsybl-core` version that matches for you.

You should also make sure that you have an implementation of `SerDe` if you are trying to read a file. See [adding SerDe](#fix-missing-serde).

### Unsupported file format or invalid file

You are most likely missing an implementation of `SerDe`. Use the implementation provided by PowSyBl / a third party, or write your own.

#### Fix missing SerDe

If you wish use to use PowSyBl's implementation, add the following to your `pom.xml` in the `<dependencies>` section:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-serde</artifactId>
    <version>$version$</version>
</dependency>
```
Please replace `$version$` by the version of this impl that will work with other PowSyBl packages.
A comprehensive table of matching versions can be found at [powsybl-dependencies](https://github.com/powsybl/powsybl-dependencies?tab=readme-ov-file#powsybl-included-repositories-versions);
use the `powsybl-core` version that matches for you.