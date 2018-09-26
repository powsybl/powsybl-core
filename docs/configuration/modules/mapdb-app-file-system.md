# Module mapdb-app-file-system

The `mapdb-app-file-system` module is used by [AFS](../../architecture/afs/README.md) to define one or several drives
mapped to a [MapDB](http://www.mapdb.org) file.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| drive-name | String | yes | - | The primary drive name |
| drive-name-X | String | no | - | The Xth secondary drive name |
| max-additional-drive-count | Integer | no | 0 | The number of secondary drives |
| remotely-accessible | Boolean | no | false | If true, the primary drive is accessible remotely |
| remotely-accessible-X | Boolean | no | false | If true, the Xth secondary is accessible remotely | 
| db-file | Path | yes | - | The primary MapDB file |
| db-file-X | Path | no | - | The Xth secondary MapDB file |

## Examples

### YAML
```yaml
local-app-file-system:
    max-additional-drive-count: 2
    drive-name: drive1
    db-file: /home/user/drive1.db
    remotely-accessible: true
    drive-name-0: drive2
    db-file-0: /home/user/drive2.db
    drive-name-1: drive3
    db-file-1: /home/user/drive3.db
    remotely-accessible-1: true
```

### XML
```xml
<local-app-file-system>
    <max-additional-drive-count>2</max-additional-drive-count>
    <drive-name>drive1</drive-name>
    <db-file>/home/user/drive1.db</db-file>
    <remotely-accessible>true</remotely-accessible>
    
    <!-- First secondary drive -->
    <drive-name-0>drive2</drive-name-0>
    <db-file-0>/home/user/drive2.db</db-file-0>
    
    <!-- Second secondary drive -->
    <drive-name-1>drive3</drive-name-1>
    <db-file-1>/home/user/drive3.db</db-file-1>
    <remotely-accessible-1>true</remotely-accessible-1>
</local-app-file-system>
```

## Reference
See also:
[mapdb-app-file-system](mapdb-app-file-system.md)
