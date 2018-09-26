# Module computation-local

The `computation-local` module is used by the `LocalComputationManager` to run computations on the local system.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| available-core | Integer | no | 1 | The maximal number of parallel computations |
| tmp-dir | List of paths | no | - | A list of paths where to store temporary files used during computations |

**tmp-dir**: The `LocalComputationManager` uses the first existing path of the list. It throws a `ConfigurationException`
if this list is empty or if none of the specified path exist. 

## Deprecated properties

**availableCore**: Deprecated since v2.1.0  
Use `available-core` instead.

**tmpDir**: Deprecated since v2.1.0.  
Use `tmp-dir` instead.

## Examples

### YAML
```yaml
computation-local:
    available-core: 1
    tmp-dir: /home/user/tmp:/tmp
```

### XML
```xml
<computation-local>
    <available-core>1</available-core>
    <tmp-dir>/home/user/tmp:/tmp</tmp-dir>
</computation-local>
```

## References
See also:
[default-computation-manager](default-computation-manager.md)
