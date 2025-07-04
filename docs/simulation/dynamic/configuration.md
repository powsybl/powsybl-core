# Configuration

## Implementation
If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file with the `default-impl-name` property.
Each implementation is identified by its name, that may be unique in the classpath:
- Use "Dynawo" to use powsybl-dynawo implementation

**YAML configuration:**
```yaml
dynamic-simulation:
  default-impl-name: Mock
```

**XML configuration:**
```xml
<dynamic-simulation>
  <default-impl-name>Mock</default-impl-name>
</dynamic-simulation>
```

## Parameters
The `dynamic-simulation-default-parameters` module is used every time a dynamic-simulation is run. It defines the default values for the most common parameters a `com.powsybl.dynamicsimulation.DynamicSimulation` implementation should be able to handle.

You may configure some generic parameters for all implementations:
```yaml
dynamic-simulation-default-parameters:
    startTime: 0
    stopTime: 1
    debugDir: /tmp/debugDir
```

The parameters may also be overridden with a JSON file, in which case the configuration will look like:
```json
{
  "version" : "1.0",
  "startTime" : 0,
  "stopTime" : 1,
  "debugDir": "/tmp/debugDir",
  "extensions" : {
    ...
  }
}
```

### Optional properties

**startTime**  
`startTime` defines when the simulation begins, in seconds. The default value of this property is `0`.

**stopTime**  
`stopTime` defines when the simulation stops, in seconds. The default value of this property is `1`.

**debugDir**
This property specifies the directory path where debug files will be dumped. If `null`, no file will be dumped.

### Specific parameters
Some implementations use specific parameters that can be defined in the configuration file or in the JSON parameters file:
- [Dynawo](inv:powsybldynawo:*:*#dynamic_simulation/configuration)

### Examples

**YAML configuration:**
```yaml
dynamic-simulation-default-parameters:
  startTime: 0
  stopTime: 3600
  debugDir: /tmp/debugDir
```

**XML configuration:**
```xml
<dynamic-simulation-default-parameters>
  <startTime>0</startTime>
  <stopTime>3600</stopTime>
  <debugDir>/tmp/debugDir</debugDir>
</dynamic-simulation-default-parameters>
```