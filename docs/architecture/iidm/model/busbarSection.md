# IIDM - Busbar Section

The `com.powsybl.iidm.network.BusbarSection` interface is used to model a busbar section, which is a non impedant
element used in a node/breaker substation topology to connect equipments. In IIDM, it is a sub interface of [Injection](injection.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| V | double | no | - | The voltage magnitude of the busbar section |
| Angle | double | no | - | The voltage angle of the busbar section |

## Examples
This example shows how to create a new Busbar Section in a network:
```java
BusbarSection bbs = voltageLevel.getNodeBreakerView().newBusbarSection()
    .setId('BBS')
    .setNode(1)
    .add();
```

## References
See also:
- [Injection](injection.md)
- [Voltage Level](voltageLevel.md)