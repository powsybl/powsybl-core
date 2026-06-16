# componentDefaultConfig
The `componentDefaultConfig` module is used to configure the implementation of plugins that the framework has to use for specific features (e.g. computation, etc.). Contrary to the other modules, it is impossible to give an exhaustive list of the existing properties.

The names of the properties are the names of Java interfaces of the powsybl framework. Each value must be the complete name of a class which implements this interface.
- ContingenciesProviderFactory
- MpiStatisticsFactory

## ContingenciesProviderFactory implementations

The `com.powsybl.contingency.EmptyContingencyListProvider` is a special implementation of the `ContingenciesProvider` interface that provides an empty list of contingencies. This
implementation should be use to check violations on a N-state.

Other implementations:
- `com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory`
- `com.powsybl.contingency.dsl.GroovyDslContingenciesProviderFactory`

```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.contingency.EmptyContingencyListProviderFactory
```
```xml
<componentDefaultConfig>
    <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
</componentDefaultConfig>
```

## Example
In the configuration below, we define these functionalities:
 - A description of contingencies

The chosen implementation is:
 - The contingencies expressed in Groovy DSL language

**YAML configuration:**
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
```

**XML configuration**
```xml
<componentDefaultConfig>
    <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
</componentDefaultConfig>
```

## Removed properties

**load-flow-factory:**<br>
The `load-flow-factory` property has been removed in PowSyBl 3.0.0. Use the `default-impl-name` property of the [load-flow](../../simulation/loadflow/configuration.md#implementation) module instead.

**SensitivityComputationFactory**<br>
The `SensitivityComputationFactory` property has been removed in PowSyBl 3.0.0. Use the [sensitivity-analysis](../../simulation/sensitivity/configuration.md) module instead.

**SensitivityFactorsProviderFactory**<br>
The `SensitivityFactorsProviderFactory` property has been removed in PowSyBl 3.0.0. Use the [sensitivity-analysis](../../simulation/sensitivity/index.md#sensitivity-factors) module and properties instead.

**SecurityAnalysisFactory**<br>
The `SecurityAnalysisFactory` property has been removed in PowSyBl 3.0.0. Use the [security-analysis](../../simulation/security/configuration.md) module instead.


