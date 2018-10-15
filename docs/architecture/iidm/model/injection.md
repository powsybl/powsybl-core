# IIDM - Injection

The `com.powsybl.iidm.network.Injection` class is used to model an equipment with a single [terminal](terminal.md).

## Creation
Before creating a subclass of Injection, its terminal must be set with the method `setNode(int)` in a Node/Breaker
Topology or `setConnectableBus(String)` and eventually `setBus(String)` in a Bus/Breaker Topology.

## Reference
See also:
- [Connectable](connectable.md)