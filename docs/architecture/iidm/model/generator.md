# IIDM - Generator

The `com.powsybl.iidm.network.Generator` interface is used to model a generator.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| EnergySource | `EnergySource` | yes | `OTHER` | The energy source |
| MinP | double | yes | - | Minimal active power |
| MaxP | double | yes | - | Maximum active power |
| RegulatingTerminal | `TerminalExt` | no | - | The terminal used for regulation |
| VoltageRegulatorOn | boolean | yes | - | The voltage regulator status |
| TargetP | double | yes | - | The active power target |
| TargetQ | double | no | - | The reactive power target |
| TargetV | double | no | - | The voltage target |
| RatedS | double | yes | - | The rated nominal power |

### EnergySource
The `com.powsybl.iidm.network.EnergySource` enum contains these six values:
- HYDRO
- NUCLEAR
- WIND
- THERMAL
- SOLAR
- OTHER

### Active Limits
The minimal active power is expected to be lower than the maximal active power.

### Targets
The voltage target is required if the voltage regulator is on.
The reactive power target is required if the voltage regulator is off.

## Examples
This example shows how to create a new Generator in the network:
```java
Generator generator = network.getVoltageLevel('VL').newGenerator()
    .setId('GEN')
    .setNode(1)
    .setEnergySource(EnergySource.HYDRO)
    .setMinP(0.0)
    .setMaxP(70.0)
    .setVoltageRegulatorOn(false)
    .setTargetP(0.0)
    .setTargetV(0.0)
    .setTargetQ(0.0)
    .add();
```

## References
See also:
- [Injection](injection.md)

