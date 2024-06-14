# Dynamic security analysis

```{toctree}
:hidden:
configuration.md
parameters.md
```

The dynamic security analysis is a [security analysis](../security/index.md) using dynamic models associated with static equipments of the network.

## Inputs

### Dynamic models mapping
The dynamic models mapping is used to associate dynamic models to static equipments of the network or add dynamic automation systems.
For the moment, the only way to associate dynamic models to static components is through a groovy script. Note that the syntax of this script is specific to each simulator:
- [Dynawo dynamic model DSL](TODO)

### Others inputs
Beside dynamic models mapping, the dynamic security analysis requires the same input as the regular one. Others inputs can be found [here](../security/index.md#inputs).

## Outputs
The dynamic security analysis produces the same output as the regular one. All outputs can be found [here](../security/index.md#outputs)

## Implementations
For the moment, the only available implementation is provided by powsybl-dynawo, which links PowSyBl with [Dynaωo](http://dynawo.org) open source suite.

## Going further
- Security analysis [Action DSL](../security/action-dsl.md).
- Security analysis [Contingency DSL](../security/action-dsl.md).
- [Run a dynamic security analysis through an iTools command](../../user/itools/dynamic-security-analysis.md): Learn how to perform a security analysis from the command line. 
