# componentDefaultConfig
The `componentDefaultConfig` module is used to configure the implementation of plugins the framework has to use for specific features (e.g. computation, etc.). Contrary to the other modules, it is impossible to give an exhaustive list of the existing properties.

The names of the properties are the names of Java interfaces of the powsybl framework. Each value must be the complete name of a class which implements this interface.
- ContingenciesProviderFactory
- SensitivityComputationFactory
- SensitivityFactorsProviderFactory
- MpiStatisticsFactory
- SecurityAnalysisFactory
- SimulatorFactory

## Example
In the configuration below, we define these functionalities:
 - A security analysis
 - A description of contingencies

The chosen implementations are:
 - "slow" security analysis (for few contingencies), post-contingency LF based implementation
 - the contingencies expressed in Groovy DSL language

**YAML configuration:**
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
```

**XML configuration**
```xml
<componentDefaultConfig>
    <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
    <SecurityAnalysisFactory>com.powsybl.security.SecurityAnalysisFactoryImpl</SecurityAnalysisFactory>
</componentDefaultConfig>
```

## Removed properties

**load-flow-factory:**  
The `load-flow-factory` property has been removed in PowSyBl 3.0.0. Use the `default-impl-name` property of the [load-flow](load-flow.md) module instead.

