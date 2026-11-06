# Runtime configuration

The runtime configuration lets an application ship its own default values in a jar, instead of
requiring every user to write them into their own configuration file.

Values contributed this way are used only for the properties the user has not defined. The full
order of precedence is:

1. the options given on the command line, for `itools` commands;
2. the environment variables, in the form `MODULE_NAME__PROPERTY_NAME`;
3. the configuration files of the directories listed in `powsybl_config_dirs`;
4. **the runtime configuration described on this page**;
5. the default values written in the Java code.

Resolution happens property by property, not module by module: a module defined both in the user
configuration file and in the runtime configuration exposes the union of their properties, and the
user's value wins wherever both define the same one.

## Shipping default values in a jar

Add a `com/powsybl/config/config.yml` resource to your jar. It uses the same syntax as a user
configuration file:

```yaml
load-flow-default-parameters:
    dc: true
    balanceType: PROPORTIONAL_TO_LOAD
import-export-parameters-default-value:
    iidm.export.xml.indent: false
```

Nothing else is required: the resource is found on the class path automatically.

### Several jars

Several jars may each carry the resource, which is how an organisation ships company-wide defaults
alongside the defaults of a given application. Each file may rank itself in the reserved
`powsybl-runtime-config` module:

```yaml
powsybl-runtime-config:
    priority: 100
```

The default priority is 0 and the higher priority wins. Files of equal priority are ordered by the
URL of their resource, so the resulting configuration never depends on the order of the class path.
The `powsybl-runtime-config` module name is reserved in `com/powsybl/config/config.yml` files: it is
removed from the configuration and cannot be read back as an ordinary module there. A Java
`RuntimeConfigProvider` exposing a module of that name is not filtered and is not affected by this
reservation.

## Computing values at runtime

When the values cannot be fixed at packaging time — they come from a licence, a deployment
descriptor or a remote service — implement `com.powsybl.commons.config.RuntimeConfigProvider`
instead:

```java
@AutoService(RuntimeConfigProvider.class)
public class MyRuntimeConfigProvider implements RuntimeConfigProvider {

    @Override
    public String getName() {
        return "my-application";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Optional<ModuleConfigRepository> getModuleConfigRepository() {
        InMemoryModuleConfigRepository repository =
                new InMemoryModuleConfigRepository(FileSystems.getDefault());
        repository.createModuleConfig("load-flow-default-parameters")
                .setStringProperty("balanceType", computeBalanceType());
        return Optional.of(repository);
    }
}
```

`getModuleConfigRepository` is called once per JVM, on the first access to the configuration.
Returning `Optional.empty()` contributes nothing. Providers are ordered by `getPriority()`, higher
first, and those of equal priority are ordered by name.

The priority declared inside a `com/powsybl/config/config.yml` file ranks these files against each
other only. The provider that reads them takes part in the ordering of the providers with the
default priority of 0, so a Java provider that must sit above or below every file declares a
positive or a negative `getPriority()`.

A `RuntimeConfigProvider` must not call `PlatformConfig.defaultConfig()` from
`getModuleConfigRepository()`: that method runs while the default platform configuration is still
being built, so the call recurses until a `StackOverflowError`.

## Diagnostics

The providers that contributed are logged at `INFO` level, in decreasing order of precedence, when
the configuration is first read.

When the same property is defined by more than one provider, or by more than one
`com/powsybl/config/config.yml` resource (two jars shipping `load-flow-default-parameters.dc` for
instance), a `WARN` line names the property, the two sources and the one that takes precedence.
Look for it first when a value is not the one you expected.

To find out whether a surprising value comes from a jar, start the application with:

```
-Dpowsybl.config.runtime.enabled=false
```

which ignores every runtime configuration provider.

## Caveat: modules whose presence is meaningful

A few classes behave differently depending on whether a module exists at all, rather than on the
value of one of its properties. `AmplConfig` for instance fails when the `ampl` module is absent.
Declaring such a module in the runtime configuration changes which branch is taken. This is
normally what you want — it is how an application ships the `ampl` home directory — but be aware
that declaring a module only partially replaces the "module is missing" error with an error about
the missing property.

## Known limitation: deprecated property aliases

Four load flow properties still accept a deprecated alias: `useReactiveLimits` (alias
`noGeneratorReactiveLimits`), `twtSplitShuntAdmittance` (`specificCompatibility`),
`shuntCompensatorVoltageControlOn` (`simulShunt`) and `componentMode` (`connectedComponentMode`).
Aliases are resolved after the layers are flattened, so a bundled value under the *new* name wins
over a user configuration that still uses the *old* name, whatever the precedence of the layers.
Integrators should ship the new names, and users should migrate their own files to them.

## Known limitation: Java modules

The resource path maps to the package `com.powsybl.config`. In a named Java module, a resource whose
path maps to a package is encapsulated and is not visible from outside the module, so a jar built as
a named module has to open that package for its configuration to be read. powsybl-core declares no
`module-info`, so this only concerns jars which are modularized.
