---
layout: default
---

# computation-local
The `computation-local` module is used by the `com.powsybl.computation.local.LocalComputationManager` to run computations on the local host, if it is configured in the [default-computation-manager](default-computation-manager.md) module.

## Optional properties

**available-core**  
The `available-core` property is an optional property that defines the maximum number of parallel computations. The default value of this property is `1`. To use all the processors of the system, set this property to `0`.

**tmp-dir**  
The `tmp-dir` property is an optional property that defines a list of paths where the temporary files generated during the computations can be stored. The temporary files will be generated in the first existing path of this list. If none of the paths exists, a `ConfigurationException` is thrown. The default value of this property is initialized with the `java.io.tmpdir` JVM system property.

## Deprecated properties

**availableCore**  
The `availableCore` property is deprecated since v2.1.0. Use the `available-core` property instead.

**tmpDir**  
The `tmpDir` property is deprecated since v2.1.0. Use the `tmp-dir` property instead.

## Examples

**YAML configuration:**
```yaml
computation-local:
    available-core: 1
    tmp-dir:
      - /home/user/tmp
      - /tmp
```

**XML configuration:**
```xml
<computation-local>
    <available-core>1</available-core>
    <tmp-dir>/home/user/tmp:/tmp</tmp-dir>
</computation-local>
```
