# IIDM - Shunt

The `com.powsybl.iidm.network.ShuntCompensator` interface is used to model a shunt compensator.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| bPerSection | double | yes | - | Positive sequence shunt (charging) susceptance per section |
| MaximumSectionCount| int | yes | - | The maximum number of sections that may be switched on |
| CurrentSectionCount | int | yes | - | The current number of section that may be switched on |

### Section
A section of a shunt compensator is an individual capacitor or reactor.

###Current Section Count
It is expected to be greater than one and lesser or equal to the Maximum Section Count.

## Examples

This example shows how to create a new ShuntCompensator in the network:
```java
ShuntCompensator shunt = network.getVoltageLevel('VL').newShunt()
    .setId('SHUNT')
    .setBus('BUS1')
    .setConnectableBus('BUS1')
    .setbPerSection(5.0)
    .setCurrentSectionCount(6)
    .setMaximumSectionCount(10)
    .add();
```

## References
See also:
- [Injection](injection.md)

