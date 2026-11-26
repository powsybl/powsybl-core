# Network modifications

The `powsybl-iidm-modification` module gathers classes and methods used to modify the network easily.
Each modification must first be created with the right attributes or parameters and then applied on the network.
A `NetworkModification` offers a method to check whether its application would have an impact on the given network.

```{toctree}
---
hidden: true
maxdepth: 1
---

network_modifications/scaling.md
network_modifications/topology_modifications.md
network_modifications/tripping.md
network_modifications/other_modifications.md
```
