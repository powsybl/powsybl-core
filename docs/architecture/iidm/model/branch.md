# IIDM - Branch

The `com.powsybl.iidm.network.Branch` interface is used to model an equipment connected to two [terminals](terminal.md). The `Branch` interface has two sub interfaces:
- [Line](line.md) to model an AC line
- [TwoWindingsTransformer](twoWindingsTransformer.md)

## Creation
Before creating an instance of a sub interface of Branch, its two terminal must be set by the methods `setVoltageLevel1(String)`, `setVoltageLevel2(String)` and
- either `setNode1(int)` and `setNode2(int)`
- or `setConnectableBus1(String)` and `setConnectableBus2(String)` and eventually `setBus1(String)` and `setBus2(String)` if the connectable buses are connected

## References
See also:
- [Connectable](connectable.md)
- [Terminal](terminal.md)
