# IIDM - Bus

The `com.powsybl.iidm.network.Bus` interface is used to model a bus, which is a set of equipments connected together through a closed [switch](switch.md).
It can be a configured object or a result of a computation depending of the context and the topology.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| V | double | no | - | The voltage magnitude of the bus |
| Angle | double | no | - | The voltage angle of the bus |

## Examples
This example shows how to create a new Bus in a network:
```java
Bus bus = voltageLevel.getBusBreakerView().newBus()
    .setId('B1')
    .add();
```

## References
See also:
- [Voltage Level](voltageLevel.md)