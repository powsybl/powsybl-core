# Configuration

## Implementation
If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file with the `default-impl-name` property.
Each implementation is identified by its name, that may be unique in the classpath:
- Use "Dynawo" to use powsybl-dynawo implementation

**YAML configuration:**
```yaml
dynamic-security-analysis:
  default-impl-name: Mock
```

**XML configuration:**
```xml
<dynamic-security-analysis>
  <default-impl-name>Mock</default-impl-name>
</dynamic-security-analysis>
```

## Parameters
The `dynamic-security-analysis-default-parameters` module is used every time a dynamic security analysis is run. It defines the default values for the most common parameters a `com.powsybl.security.dynamic.DynamicSecurityAnalysis` implementation should be able to handle.
In addition to its own set of parameters, the dynamic security analysis reuses [dynamic simulation parameters](../dynamic/configuration.md).

You may configure some generic parameters for all implementations:
```yaml
dynamic-simulation-default-parameters:
  startTime: 0
  stopTime: 100

dynamic-security-analysis-default-parameters:
  contingencies-start-time: 10
  debugDir: /tmp/debugDir
```

The parameters may also be overridden with a JSON file, in which case the configuration will look like:
```json
{
  "version" : "1.0",
  "dynamic-simulation-parameters" : {
    "version" : "1.0",
    "startTime" : 0.0,
    "stopTime" : 20.5
  },
  "contingencies-parameters" : {
    "contingencies-start-time" : 5.5
  },
  "debugDir": "/tmp/debugDir"
}
```

### Optional properties

**contingencies-start-time**  
`contingencies-start-time` defines when the contingencies start, in seconds. The default value of this property is `5`.

**debugDir**
This property indicates a directory path where debug files will be dumped. If `null`, no file is dumped.

### Examples

**YAML configuration:**
```yaml
dynamic-security-analysis-default-parameters:
  contingencies-start-time: 10
  debugDir: /tmp/debugDir
```

**XML configuration:**
```xml
<dynamic-security-analysis-default-parameters>
  <contingencies-start-time>10</contingencies-start-time>
  <debugDir>/tmp/debugDir</debugDir>
</dynamic-security-analysis-default-parameters>
```