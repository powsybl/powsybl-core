# IIDM - Voltage level

The `com.powsybl.iidm.network.VoltageLevel` is used to model a voltage level. A voltage level is is a collection of
equipments located in the same substation and at the same base voltage.
It can contain generators, loads, shunt compensators, dangling lines, static VAR compensators and HVDC
converter stations.

A voltage level is located in a substation.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| id | String | yes | - | The ID of the voltage level |
| name | String | no | - | The name of the voltage level |
| NominalV | double | yes | - | The nominal voltage |
| LowVoltageLimit | double | no | - | The low voltage limit |
| HighVoltageLimit | double | no | - | The high voltage limit |
| TopologyKind | `TopologyKind` | yes | - | The kind of topology |

### TopologyKind
`TopologyKind` describes the topology model of the voltage level i.e. how equipments are connected together.

The `com.powsybl.iidm.network.TopologyKind` enum contains these two values:
- NODE_BREAKER
- BUS_BREAKER

NODE_BREAKER corresponds to a **node/breaker model**, which is the most detailed way to describe a topology. All
elements are physical ones: busbar sections, breakers and disconnectors. A node in a node/breaker context means
"connection node" and not topological node or bus.

BUS_BREAKER corresponds to a **bus/breaker model**, which is an aggregated form of the topology made of buses and
breakers. A bus is the aggregation of busbar sections and closed switches.

## Example
This example shows how to create a new `VoltageLevel` object:
```java
VoltageLevel voltageLevel = substation.newVoltageLevel()
    .setId('VL')
    .setName('VL') // optional
    .setNominalV(20)
    .setTopologyKind(TopologyKind.NODE_BREAKER)
    .setLowVoltageLimit(15)
    .setHighVoltageLimit(25)
    .add();
```

## References
See also:
- [Substation](substation.md)
- [Generator] (generator.md)
- [Load] (load.md)
- [Shunt Compensator] (shuntCompensator.md)
- [Dangling Line] (danglingLine.md)
- [Static VAR Compensator] (staticVarCompensator.md)
- [HVDC Converter Station] (hvdcConverterStation.md)