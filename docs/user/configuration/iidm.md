# iidm
The `iidm` module is used to configure the in-memory IIDM network implementation.

## Optional properties

**node-index-limit**<br>
The `node-index-limit` property is an optional property that defines the maximum number of nodes allowed in the topology of a node/breaker (or bus/breaker) voltage level. Creating a node whose index reaches this limit throws an exception. Its default value is `1000`.

**dc-node-index-limit**<br>
The `dc-node-index-limit` property is an optional property that defines the maximum number of DC nodes allowed in the DC topology of the network. It plays the same role as `node-index-limit` for the detailed DC model. Its default value is `1000`.

## Examples

**YAML configuration:**
```yaml
iidm:
    node-index-limit: 1000
    dc-node-index-limit: 1000
```
**XML configuration:**
```xml
<iidm>
    <node-index-limit>1000</node-index-limit>
    <dc-node-index-limit>1000</dc-node-index-limit>
</iidm>
```
