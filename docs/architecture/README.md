# powsybl-core architecture

Powsybl-core is designed in a modular approach, to allow other projects to extend its features or to modify the default behaviours.

| Layer | Functionalities |
| ----- | --------------- |
| Business oriented functionalities | [Action simulator](), [Simulation (dynamic)]() <br/> [LoadFlow validation](), [Load-Flow](), [Security Analysis]() |
| Business data models | [IIDM (Network Modeling)](), [Converters](), [Contingencies](), [Actions]() |
| Technical base | [Commons](),  [Tools](../tools/README.md), [Computation](), [AFS (Application File System)](../../afs/README.md) |

