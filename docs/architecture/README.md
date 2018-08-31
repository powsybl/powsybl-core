# powsybl-core architecture

Powsybl-core is designed in a modular approach, to allow other projects to extend its features or to modify the default behaviours.

| Layer | Functionalities |
| ----- | --------------- |
| Business oriented functionalities | [Action simulator]() and [iAL](./ial/README.md), [Simulation (dynamic)]() <br/> [Load-Flow validation](./loadflow-validation/README.md), [Load-Flow](), [Security Analysis]() |
| Business data models | [IIDM (Network Modeling)](./iidm/README.md), [Converters](), [Contingencies](), [Actions]() |
| Technical base | [Commons](),  [Tools](../tools/README.md), [Computation](), [AFS (Application File System)](../../afs/README.md) |

