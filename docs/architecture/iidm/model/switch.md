# IIDM - Switch

The `com.powsybl.iidm.network.Switch` interface is used to a switch which connects equipments in a [voltage level](voltageLevel.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| Kind | `SwitchKind` | yes | - | The kind of switch |
| Open | boolean | yes | false | The open status of the switch |
| Retained | boolean | yes | false | The retain status of the switch |
| Fictitious | boolean | yes | false | The fictitious status of the switch |
| Node1 | int | no | - | The first node the switch is connected to |
| Node2 | int | no | - | The second node the switch is connected to |
| Bus1 | String | no | - | The first bus the switch is connected to |
| Bus2 | String | no | - | The second bus the switch is connected to |

Node1 and Node2 are required in a node/breaker topology.
Bus1 and Bus2 are required in a bus/breaker topology.

### SwitchKind
The `com.powsybl.iidm.network.SwitchKind` enum contains three values:
- BREAKER
- DISCONNECTOR
- LOAD_BREAK_SWITCH

In a bus/breaker topology, `SwitchKind` can only be set to BREAKER.

## Examples
This example shows how to create a new Switch in a network with a node/breaker topology:
```java
Switch s = voltageLevel.getNodeBreakerView().newSwitch()
    .setId('S')
    .setNode1(1)
    .setNode2(2)
    .setKind(SwitchKind.DISCONNECTOR)
    .setOpen(false)
    .setRetained(false)
    .add();
```

In a node/breaker topology, a switch can also be created with the function `newDisconnector()` or `newBreaker()` if it is respectively
a disconnector or a breaker:
```java
Switch s = voltageLevel.getNodeBreakerView().newDisconnector()
    .setId('SD')
    .setNode1(3)
    .setNode2(4)
    .setOpen(false)
    .setRetained(false)
    .add();

Switch s = voltageLevel.getNodeBreakerView().newBreaker()
    .setId('SB')
    .setNode1(5)
    .setNode2(6)
    .setOpen(false)
    .add();
```

This example shows how to create a new Switch in a network with a bus/breaker topology:
```java
Switch s = voltageLevel.getBusBreakerView().newSwitch()
    .setId('S')
    .setBus1('B1')
    .setBus2('B2')
    .setOpen(false)
    .add();
```

## References
See also:
- [Voltage Level](voltageLevel.md)
