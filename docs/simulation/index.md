# Simulation

One major aim of the PowSyBl project is to make it easy to plug it with different simulators or optimization tools for grid calculations. At the moment, several types of calculations are supported: load flow, security analysis, sensitivity analysis, short-circuit analysis, or time-domain simulation (also called dynamic simulation). 

For each of them, one or several simulators are supported, but it is also possible to plug your own simulator within the library.
Below comes a description of the various types of simulation. 


```{toctree}
---
maxdepth: 1
---

loadflow/index.md
security/index.md
sensitivity/sensitivity.md
shortcircuit/index.md
dynamic/index.md
```