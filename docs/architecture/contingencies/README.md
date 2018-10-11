# Contingencies

The `com.powsybl.contingency.Contingency` class is used to model a set of equipments that are disconnected, because of a
maintenance work or a technical issue.

A `Contingency` is identified by its ID and can contain one or several `ContingencyElement`. These elements references an
equipment in the network. The supported type of equipements are:
- generators
- branches (lines, tie-lines or two windings transformers)
- HVDC lines
- Busbar sections

## N-1 contingency
A N-1 contincency is a contingency that triggers a single equipement.

## N-k contingency
A N-k contingency is a contingency that triggers several equipements.

## Busbar contingecy
A busbar contingency is a contingency that triggers a busbar section. This a subtype of the N-k contingency because this
kind of contingencies will trigger every equipments connected to the specified busbar section. 

## References
- [Generator](../iidm/model/generator.md)
- [Line](../iidm/model/line.md)
- [TieLine](../iidm/model/tieLine.md)
- [HvdcLine](../iidm/model/hvdcLine.md)
- [BusbarSection](../iidm/model/busbarSection.md)
