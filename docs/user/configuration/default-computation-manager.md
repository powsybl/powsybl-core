---
layout: default
---

# default-computation-manager
The `default-computation-manager` module is an optional module loaded when an `iTools` command starts, to determine which `com.powsybl.computation.ComputationManager` implementation should be used for short-time and long-time computations. The choice of using the short-time or the long-time computation manager factory is done by the implementation of each type of computation (e.g. load-flow, security-analysis, etc.).

If this module is not set, the `com.powsybl.computation.local.LocalComputationManager` implementation is used. Read the [computation-local](computation-local.md) page to learn how to configure the `LocalComputationManager`.

## Required properties

**short-time-execution-computation-manager-factory**  
The `short-time-execution-computation-manager-factory` property is a required property that defines the name of the `com.powsybl.computation.ComputationManagerFactory` implementation to use for short-time computations.

## Optional properties

**long-time-execution-computation-manager-factory**  
The `long-time-execution-computation-manager-factory` property is an optional property that defines the name of the `com.powsybl.computation.ComputationManagerFactory` implementation to use for long-time computations. If not defined, this property returns the same value as the `short-time-execution-manager-factory`.

## Examples

**YAML configuration:**
```yaml
default-computation-manager:
    long-time-execution-computation-manager-factory: com.powsybl.computation.local.LocalComputationManagerFactory
    short-time-execution-computation-manager-factory: com.powsybl.computation.local.LocalComputationManagerFactory
```

**XML configuration:**
```xml
<default-computation-manager>
    <long-time-execution-computation-manager-factory>com.powsybl.computation.local.LocalComputationManagerFactory</long-time-execution-computation-manager-factory>
    <short-time-execution-computation-manager-factory>com.powsybl.computation.local.LocalComputationManagerFactory</short-time-execution-computation-manager-factory>
</default-computation-manager>
```
