# IIDM - Load

The `com.powsybl.iidm.network.Load` interface is used to model a constant power load. The active and reactive power
setpoints are fixed.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| LoadType | `LoadType` | no | `UNDEFINED` | The type of the load |
| P0 | double | yes | - | The active power setpoint |
| Q0 | double | yes | - | The reactive power setpoint |

### LoadType
The `com.powsybl.iidm.network.LoadType` enum contains these three values:
- UNDEFINED
- AUXILIARY
- FICTITIOUS

## Examples

This example shows how to create a new Load in the network:
```java
Load load = network.getVoltageLevel('VL').newLoad()
    .setId('LOAD')
    .setBus('BUS1')
    .setConnectableBus('BUS1')
    .setLoadType(LoadType.UNDEFINED)
    .setP0(100.0)
    .setQ0(60.0)
    .add();
```

## References
See also:
- [Injection](injection.md)
