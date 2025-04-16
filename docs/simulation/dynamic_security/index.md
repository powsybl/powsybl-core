# Dynamic security analysis

```{toctree}
:hidden:
configuration.md
```

The dynamic security analysis is a [security analysis](../security/index.md) using dynamic models associated with static equipment of the network.

## Inputs

### Dynamic models configuration
The dynamic models configuration is exactly the same [configuration](../dynamic/index.md#dynamic-models-configuration) used for a dynamic simulation.

### Other inputs
Besides dynamic models configuration, the dynamic security analysis requires the same [inputs as the standard one](../security/index.md#inputs).

## Outputs
The dynamic security analysis produces the same outputs as the standard one. All outputs can be found [here](../security/index.md#outputs).

## Implementations
For the moment, the only available implementation is provided by powsybl-dynawo, which links PowSyBl with [Dynaωo](http://dynawo.org) open source suite.

## Going further
- Security analysis [Action DSL](../security/action-dsl.md).
- Security analysis [Contingency DSL](../security/action-dsl.md).
- [Run a dynamic security analysis through an iTools command](../../user/itools/dynamic-security-analysis.md): Learn how to perform a security analysis from the command line. 
- [List dynamic simulation models with an iTools command](../../user/itools/list-dynamic-simulation-models.md): learn how to load a list of all dynamic simulation models from the command line.