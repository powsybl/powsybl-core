# Simulation

One major aim of the PowSyBl project is to make it easy to plug it with different simulators or optimisation tools for
grid calculations. At the moment, several types of calculations are supported: load flow, security analysis, sensitivity analysis,
time-domain simulation (also called dynamic simulation) or short-circuit analysis. For each of them,
one or several simulators is officially supported, but it is also possible to plug your own simulator within the library.
Below comes a description of the various types of simulation and supported implementations. PowSyBl also supports
optimal power flows with just an implementation such as Metrix or OpenReac.

```{toctree}
---
maxdepth: 2
---

loadflow/index.md
security/security.md
sensitivity/sensitivity.md
shortcircuit/index.md
```