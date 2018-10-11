# Contingencies - EmptyContingencyListProvider

The `com.powsybl.contingency.EmptyContingencyListProvider` is a special implementation of the
[ContingenciesProvider](contingenciesProvider.md) interface that provided an empty list of contingencies. This implementation
should be use to check violations on a N-state.

## Configuration

To use the `EmptyContingencyListProvider`, configure the `componentDefaultConfig` module in the
[configuration file](../../configuration/configuration.md):

### YAML
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.contingency.EmptyContingencyListProviderFactory
```

### XML
```xml
<componentDefaultConfig>
    <ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
</componentDefaultConfig>
```

## References
See also:
[componentDefaultConfig](../../configuration/modules/componentDefaultConfig.md),
[ContingenciesProvider](contingenciesProvider.md),
[GroovyDSLContingenciesProvider](groovyDslContingenciesProvider),
[security-analysis](../../tools/security-analysis.md)
