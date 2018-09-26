# Module local-app-file-system

The `local-app-file-system` module is used by [AFS](../../architecture/afs/README.md) to define one or several drives
mapped to a local hard-drive.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| drive-name | String | yes | - | The primary drive name |
| drive-name-X | String | no | - | The Xth secondary drive name |
| max-additional-drive-count | Integer | no | 0 | The number of secondary drives |
| remotely-accessible | Boolean | no | false | If true, the primary drive is accessible remotely |
| remotely-accessible-X | Boolean | no | false | If true, the Xth secondary is accessible remotely | 
| root-dir | Path | yes | - | The primary root directory |
| root-dir-X | Path | no | - | The Xth secondary root directory |

**root-dir**: If the specified path does not exist, an `AfsException` is thrown.

## Examples

### YAML
```yaml
local-app-file-system:
    max-additional-drive-count: 2
    drive-name: drive1
    root-dir: /home/user/drive1
    drive-name-0: drive2
    root-dir-0: /home/user/drive2
    drive-name-1: drive3
    root-dir-1: /home/user/drive3
```

### XML
```xml
<local-app-file-system>
    <max-additional-drive-count>2</max-additional-drive-count>
    <drive-name>drive1</drive-name>
    <root-dir>/home/user/drive1</root-dir>
    <remotely-accessible>true</remotely-accessible>
    
    <!-- First secondary drive -->
    <drive-name-0>drive2</drive-name-0>
    <root-dir-0>/home/user/drive2</root-dir-0>
    
    <!-- Second secondary drive -->
    <drive-name-1>drive3</drive-name-1>
    <root-dir-1>/home/user/drive3</root-dir-1>
    <remotely-accessible-1>true</remotely-accessible-1>
</local-app-file-system>
```

## Reference
See also:
[mapdb-app-file-system](mapdb-app-file-system.md)
