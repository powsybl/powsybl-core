# Short-circuit analysis

When a short circuit occurs in a network, the currents on equipment can be so high that they exceed their rated values.
Simulating faults on the network is important to verify that the short circuits are well detected and do not damage the equipment.

The short-circuit API allows the calculation of currents and voltages on a network after a fault.
A first implementation of the API is available in [powsybl-incubator](https://github.com/powsybl/powsybl-incubator/tree/main/simulator/short-circuit).

```{toctree}
---
caption: Short-circuit analysis
maxdepth: 2
hidden: true
---

parameters.md
inputs.md
outputs.md
```
