# Java module system (JPMS) support

Most powsybl-core jars ship a `module-info.java` descriptor, so they can be used on the module path as well as on the classpath.

## Module names

The module name is the former `Automatic-Module-Name` (e.g. `com.powsybl.commons`, `com.powsybl.iidm.api`), so existing automatic-module users are not impacted.

Modules without a descriptor keep their `Automatic-Module-Name` in the manifest: `powsybl-itools-packager` (Maven plugin), and the Groovy-only or resource-only modules (`action-ial-util`, `loadflow-scripting`, `cgmes-model-alternatives`, `iidm-tck`, `psse-model-test`, `triple-store-test`).

`test-jar` artifacts cannot be real modules (they share packages with their main jar). They only declare an `Automatic-Module-Name` ending with `.test` (e.g. `com.powsybl.computation.api.test`).

## Services

`provides ... with` and `uses` are declared in the descriptors and mirror the `META-INF/services` files, which are still shipped for classpath usage. Generic loaders (`ServiceLoaderCache`, `PlatformConfigNamedProvider`, `GroovyExtension`) call `Module.addUses` at runtime since they cannot know the service types statically.

## Resources

A package cannot exist in two modules, so resource folders are package-qualified:

- XIIDM extension schemas from `iidm-serde` and `iidm-extensions` are all in `/xsd` (in `iidm-serde`).
- `entsoe-util` and `cgmes-extensions` schemas are next to their serializers (e.g. `com/powsybl/entsoe/util/entsoeArea.xsd`).

## Known limitations

- Some third-party dependencies are not modules and have no `Automatic-Module-Name`: `re2j`, `stax-utils`, `trove4j`, `sirocco-text-table-formatter` and the `rdf4j` jars. They are required under their file-name-based automatic module names (marked `// No Automatic-Module-Name` in the descriptors); if upstream changes a jar name, the corresponding `requires` must be updated. The compiler warns about this at build time.
- `powsybl-triple-store-impl-rdf4j` cannot be launched on the module path, because the rdf4j jars have split packages between them. Use the classpath for it.

## Building and testing

Main code is compiled on the module path. Tests are compiled and run on the classpath (`useModulePath=false`), as they rely on `@AutoService` providers and on test-jars sharing packages with their main module. In IntelliJ, choose *Use classpath only* in the run configuration, or delegate to Maven.
