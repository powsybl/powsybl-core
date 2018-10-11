# Contingencies - ContingenciesProvider

The `com.powsybl.contingency.ContingenciesProvider` interface is used to provide a list of `Contingency` for the
[security-analysis](../../tools/security-analysis.md) and [action-simulator](../../tools/action-simulator.md) iTools commands.

Powsybl provides several implementation of ContingenciesProvider:
- [EmptyContingencyListProvider](emptyContingencyListProvider.md)
- [GroovyDslContingenciesProvider](groovyDslContingenciesProvider.md)
