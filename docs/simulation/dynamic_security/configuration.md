# Configuration

## Implementation
If you have several implementation in your classpath, you need to choose which implementation to use in your configuration file with the `default-impl-name` property.
Each implementation is identified by its name, that may be unique in the classpath:
- use "DynaWaltz" to use powsybl-dynawo implementation

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
In addition to its own set of parameter, the dynamic security analysis reuse [dynamic simulation parameters](../dynamic/parameters.md).

You may configure some generic parameters for all implementations:
```yaml
dynamic-simulation-default-parameters:
  startTime: 0
  stopTime: 100

dynamic-security-analysis-default-parameters:
  contingencies-start-time: 10
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
  }
}
```

### Optional properties

**contingencies-start-time**  
`contingencies-start-time` defines when the contingencies start, in seconds. The default value of this property is `5`.

### Examples

**YAML configuration:**
```yaml
dynamic-security-analysis-default-parameters:
  contingencies-start-time: 10
```

**XML configuration:**
```xml
<dynamic-security-analysis-default-parameters>
  <contingencies-start-time>10</contingencies-start-time>
</dynamic-security-analysis-default-parameters>
```