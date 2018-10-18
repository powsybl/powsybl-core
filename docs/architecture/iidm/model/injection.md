# IIDM - Injection

The `com.powsybl.iidm.network.Injection` class is used to model an equipment with a single [terminal](terminal.md).
In IIDM, the Injection interface has seven sub interfaces:
- [Busbar Section](busbarSection.md)
- [Dangling Line](danglingLine.md)
- [Generator](generator.md)
- [HVDC Converter Station](hvdcConverterStation.md)
- [Load](load.md)
- [Shunt Compensator](shuntCompensator.md)
- [Static VAR Compensator](staticVarCompensator.md)

## Creation
Before creating an instance of a sub interface of Injection, its terminal must be set with the method `setNode(int)` in a Node/Breaker
Topology or `setConnectableBus(String)` and eventually `setBus(String)` in a Bus/Breaker Topology.

## Reference
See also:
- [Connectable](connectable.md)
- [Terminal](terminal.md)