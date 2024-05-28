---
layout: default
---

# Parameters
The `dynamic-simulation-default-parameters` module is used every time a dynamic-simulation is run. It defines the default values for the most common parameters a `com.powsybl.dynamicsimulation.DynamicSimulation` implementation should be able to handle. 

## Optional properties

**startTime**  
The `startTime` property is an optional property that defines the instant of time at which the dynamic simulation begins, in seconds. The default value of this property is `0`.

**stopTime**  
The `stopTime` property is an optional property that defined the instant of time at which the dynamic simulation ends, in seconds. The default value of this property is `1`.

## Examples

**YAML configuration:**
```yaml
dynamic-simulation-default-parameters:
  startTime: 0
  stopTime: 3600
```

**XML configuration:**
```xml
<dynamic-simulation-default-parameters>
  <startTime>0</startTime>
  <stopTime>3600</stopTime>
</dynamic-simulation-default-parameters>
```
