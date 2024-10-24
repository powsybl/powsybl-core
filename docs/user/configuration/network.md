---
layout: default
---

# network
The `network` module is used to configure the network default implementation name. The network implementation is the set of classes implementing all the network elements, such as VoltageLevel or Generator. The implementation named "Default" is the classic PowSyBl in-memory implementation.

## Required properties

**default-impl-name**  
The `default-impl-name` property is a required property that specifies the name of the default network implementation.

## Examples

**YAML configuration:**
```yaml
network:
    default-impl-name: Default
```

**XML configuration:**
```xml
<network>
    <default-impl-name>Default</default-impl-name>
</network>
```
